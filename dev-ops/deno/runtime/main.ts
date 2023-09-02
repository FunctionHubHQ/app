import { Application, isHttpError } from "https://deno.land/x/oak/mod.ts";
import Queue from "npm:queue-fifo"

interface ThreadStatus {
  updatedAt?: number,
  cpuTime?: number,
  memoryUsage?: number,
  cpuThreshold?: number,
  memoryThreshold?: number,
  cpuBaseLine?: number
}

const devHost = 'http://host.docker.internal:9090';
const prodHost = 'http://api:9090';

/**
 * Keeps track of console logs from user scripts workers
 */
const stdout = {}
const workersPendingId = new Queue();
const activeWorkers = new Map();// The key is a unique thread id and the value is the uid from the request body
const threadStatus = new Map(); // inverted mapping of activeWorkers
const workerStartTimes = new Map();

function getHost(env) {
  return env === "prod" ? prodHost : devHost
}

async function getBody(ctx) {
  let body;
  try {
    body = JSON.parse(await ctx.request.body().value);
  } catch (e) {
    body =await ctx.request.body().value;
  }
  return body;
}

function executeUserScript(ctx, userScriptUrl, body) {
  // Create a new Web Worker
  const worker = new Worker(userScriptUrl,
      {
        type: "module",
        name: body.uid
      }
  );
  workersPendingId.enqueue(body.uid)
  const _threadStat: ThreadStatus = {
    updatedAt: -1,
    cpuTime: -1,
    memoryUsage: -1,
    cpuThreshold: 10,
    memoryThreshold: 99999999999999,
    cpuBaseLine: -1
  }
  threadStatus.set(body.uid, _threadStat)

  let timeoutId, interval;
  // Listen for messages from the Web Worker
  worker.onmessage = (event) => {
    const uid = event.data.uid;
    if (event.data.stdout) {
      let prevStdout = stdout[uid];
      if (!prevStdout) {
        prevStdout = []
      }
      prevStdout.push(event.data.stdout);
      stdout[uid] = prevStdout;
    } else if (event.data.result) {
      if (timeoutId) clearTimeout(timeoutId);
      if (interval) clearInterval(interval)
      workerStartTimes.delete(event.data.uid)
      sendResult(ctx,
          event.data.result,
          [...stdout[uid]],
          null)
      .catch(e => console.log(e))
      stdout[uid] = [];
    } else if (event.data.error) {
      worker.terminate();
      if (timeoutId) clearTimeout(timeoutId);
      if (interval) clearInterval(interval)
      workerStartTimes.delete(event.data.uid)
      sendResult(ctx,
          null,
          [...stdout[uid]],
          event.data.error)
      .catch(e => console.log(e))
      stdout[uid] = [];
    } else if (event.data.startedAt) {
      console.log("Setting start time: ", event.data.uid, event.data.startedAt)
      workerStartTimes.set(event.data.uid, event.data.startedAt)
    }
  };

  // Handle errors from the Web Worker
  worker.onerror = (error) => {
    error.preventDefault();
    if (timeoutId) clearTimeout(timeoutId);
    if (interval) clearInterval(interval)
    sendResult(ctx, null,
        [...stdout[body.uid]],
        error.message)
    .catch(e => console.log(e));
    stdout[body.uid] = [];
  };

  stdout[body.uid] = [];
  worker.postMessage({uid: body.uid, payload: body.payload});

  timeoutId = setTimeout(() => {
    // Terminate the Web Worker when the timeout occurs
    worker.terminate();
    sendResult(ctx, null,
        [...stdout[body.uid]],
        "Execution timed out")
    .catch(e => console.log(e));
    delete stdout[body.uid];
    console.log(stdout);
  }, body.timeout);

  interval = setInterval(() => {
    // Terminate the Web Worker if it has exceeded its cpu or memory thresholds
    if (threadStatus.has(body.uid)) {
      const status: ThreadStatus = threadStatus.get(body.uid)
      let errorMessage = undefined
      if (status?.cpuTime > status?.cpuBaseLine) {
        const delta = status.cpuTime - status.cpuBaseLine
        if (delta > status.cpuThreshold) {
          errorMessage = "CPU timeout"
        }
      } else if (status?.memoryUsage > status?.memoryThreshold) {
        // TODO: consider setting a memory threshold, e.g. what if the initial usage ends up
        //  being greater than the threshold even before the user code could run?
        errorMessage = "Out of memory"
      }
      if (errorMessage) {
        worker.terminate();
        sendResult(ctx, null,
            [...stdout[body.uid]],
            errorMessage)
        .catch(e => console.log(e));
        workerStartTimes.delete(body.uid)
        threadStatus.delete(body.uid)
      }
    }
  }, 10);
}


async function sendResult(ctx, result, stdout, error) {
  const body = await getBody(ctx);
  const data = {
    uid: body.uid,
    exec_id: body.execId,
    validate: body.validate,
    deployed: body.deployed,
    error: error,
    result: JSON.stringify(result),
    std_out: stdout,
  }
  const url = getHost(body.env) + "/e-result";
  const sendStatus = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": "Bearer " + body.apiKey
    },
    body: JSON.stringify(data),
  });
  const rt = ctx.response.headers.get("X-Response-Time");
  console.log(`${ctx.request.method} - Egress - ${url} - ${sendStatus.status} - ${rt}ms`);
}

async function executeUserCode(ctx) {
  const body = await getBody(ctx);
  const host = getHost(body.env);
  const url = `${host}/npm/${body.uid}`;
  executeUserScript(ctx, url, body);
}

async function handleNewThreadSignal(ctx) {
  // A new thread signal arrives before the worker object has been queued
  // so we should wait for it to be queued. In a multi-tenant situation, the queue
  // should always be empty so that we know that the new thread signal
  // belongs to the first worker that gets enqueued. We ensure this by
  // deliberately throttling incoming user code execution requests so that there is
  // always enough gap for signaling, queueing, and dequeue exactly one worker
  // thread.
  const body = await getBody(ctx);
  activeWorkers.set(body.thread_id, workersPendingId.peek());
  workersPendingId.dequeue();
  ctx.response.body = {status: "ok"} ;
}

async function handleThreadAlarm(ctx) {
  const body = await getBody(ctx);
  // TODO: Remove thread_id from activeWorkers if the worker has finished
  const workerUid = activeWorkers.get(body.thread_id)
  if (workerUid) {
    // It takes about 2 seconds to teardown a worker. That is too expensive of an operation.
    // Need to improve that!
    const newUpdatedAt = body.updated_at
    // WorkerStartTime could be undefined until the worker has fully initializing. There is a 300-400
    // millisecond latency due to 'cold' starting a worker. This should be dealt with later.
    const workerStartTime = workerStartTimes.get(workerUid)
    if (workerStartTime && newUpdatedAt >= workerStartTime) {
      const prevStatus: ThreadStatus = threadStatus.get(workerUid);
      const updatedStatus: ThreadStatus = {
        // The time at which the thread was sampled at
        updatedAt: body.updated_at,

        // A baseline CPU time is the time it took to set up the worker
        cpuBaseLine: prevStatus && prevStatus.cpuBaseLine > -1 ? prevStatus.cpuBaseLine : body.cpu_time,

        // The current CPU time. This would be the same as the baseline for the first
        // update. But subsequent updates would be greater than the baseline
        cpuTime: body.cpu_time,

        // Current memory usage. Note that we don't have a memory usage baseline because
        // the memory doesn't change that much from the start of the worker till the user code
        // execution
        memoryUsage: body.memory_usage ? body.memory_usage : prevStatus.memoryUsage,

        // The worker's max allowed memory usage in bytes
        memoryThreshold: prevStatus?.memoryThreshold,

        // The worker's max allowed cpu usage in milliseconds
        cpuThreshold: prevStatus.cpuThreshold
      }
      threadStatus.set(workerUid, updatedStatus)
    }
  }
  ctx.response.body = {status: "ok"} ;
}

const app = new Application();

// Logger
app.use(async (ctx, next) => {
    await next();
    const rt = ctx.response.headers.get("X-Response-Time");
    console.log(`${ctx.request.method} - Ingress - ${ctx.request.url} - ${rt}`);
});
  
  // Timing
app.use(async (ctx, next) => {
    const start = Date.now();
    await next();
    const ms = Date.now() - start;
    ctx.response.headers.set("X-Response-Time", `${ms}ms`);
});


// Error handling middleware
app.use(async (context, next) => {
    try {
      await next();
    } catch (error) {
      if (isHttpError(error)) {
        // Handle known HTTP errors and send appropriate responses
        context.response.status = error.status;
        context.response.body = { error: error.message };
      } else {
        context.response.status = 500;
      }
      await sendResult(context, null, null, error.message);
      context.response.body = { error: error.message }
      context.response.type = "application/json";
    }
  });


  app.use(async (ctx) => {
    ctx.response.type = "application/json";
    const path = ctx.request.url.pathname;
    if (path.includes("/execute")) {
        await executeUserCode(ctx);
    } else if (path.includes("/new-thread")) {
      await handleNewThreadSignal(ctx)
    }
    else if (path.includes("/thread-alarm")) {
      await handleThreadAlarm(ctx)
    }
    else {
        ctx.response.body = {status: "FunctionHub Runtime is healthy"} ;
    }
  });

console.log("Server listening on port 8000");
await app.listen({ port: 8000 });
