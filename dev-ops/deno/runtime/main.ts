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
const activeWorkersByThreadId = new Map();// The key is a unique thread id and the value is the uid from the request body
const threadStatus = new Map(); // inverted mapping of activeWorkersByThreadId
const workerStartTimes = new Map();
const activeWorkersByWorkerId = new Map();
const exitedWorkersByWorkerId = new Map(); // Workers that have exited

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

function getWorkerId(body?: any, uid?: string, execId?: string) {
  if (body) {
    uid = body.uid
    execId = body.execId
  }
  return `${uid}:${execId}`
}

function clearIds(timeoutId, intervalId) {
  if (timeoutId) clearTimeout(timeoutId);
  if (intervalId) clearInterval(intervalId)
}

function clearWorkerId(workerId) {
  console.log((new Date()).getTime(), ": ", "clearWorkerId: ", workerId)
  workerStartTimes.delete(workerId)
  threadStatus.delete(workerId)
  const threadId = activeWorkersByWorkerId.get(workerId)
  activeWorkersByWorkerId.delete(workerId)
  if (threadId) {
    activeWorkersByThreadId.delete(threadId)
  } else {
    console.log((new Date()).getTime(), ": ", "ThreadId not found for workerId: ", workerId)
  }
}

function registerEventListener(worker, ctx, timeoutId, intervalId) {

  // Listen for messages from the Web Worker
  worker.onmessage = (event) => {
    const workerId = getWorkerId(null, event.data.uid, event.data.execId)
    // const uid = event.data.uid;
    if (event.data.stdout) {
      let prevStdout = stdout[workerId];
      if (!prevStdout) {
        prevStdout = []
      }
      prevStdout.push(event.data.stdout);
      stdout[workerId] = prevStdout;
    } else if (event.data.result) {
      clearIds(timeoutId, intervalId);
      // if (timeoutId) clearTimeout(timeoutId);
      // if (interval) clearInterval(interval)
      // workerStartTimes.delete(workerId)
      // console.log((new Date()).getTime(), ": ", "DEBUG 0: sendExecutionResult")
      sendExecutionResult(ctx,
          event.data.result,
          [...stdout[workerId]],
          null)
      .catch(e => console.log((new Date()).getTime(), ": ", e))
      stdout[workerId] = [];
    } else if (event.data.error) {
      worker.terminate();
      clearIds(timeoutId, intervalId);
      // if (timeoutId) clearTimeout(timeoutId);
      // if (interval) clearInterval(interval)
      // workerStartTimes.delete(workerId)
      // console.log((new Date()).getTime(), ": ", "DEBUG 1: sendExecutionResult")
      sendExecutionResult(ctx,
          null,
          [...stdout[workerId]],
          event.data.error)
      .catch(e => console.log((new Date()).getTime(), ": ", e))
      stdout[workerId] = [];
    } else if (event.data.startedAt) {
      console.log((new Date()).getTime(), ": ", "Setting start time: ", workerId, event.data.startedAt)
      workerStartTimes.set(workerId, event.data.startedAt)
    }
  };
}

function registerErrorListener(worker, ctx, body, timeoutId, intervalId) {
  const workerId = getWorkerId(body, null, null)
  worker.onerror = (error) => {
    error.preventDefault();
    // if (timeoutId) clearTimeout(timeoutId);
    // if (interval) clearInterval(interval)
    clearIds(timeoutId, intervalId);
    // console.log((new Date()).getTime(), ": ", "DEBUG 2: sendExecutionResult")
    sendExecutionResult(ctx, null,
        [...stdout[workerId]],
        error.message)
    .catch(e => console.log((new Date()).getTime(), ": ", e));
    stdout[workerId] = [];
  };
}

function setTimeoutHandler(worker, ctx, body) {
  const workerId = getWorkerId(body, null, null)
  return setTimeout(() => {
    // Terminate the Web Worker when the timeout occurs
    worker.terminate();
    // console.log((new Date()).getTime(), ": ", "DEBUG 3: sendExecutionResult")
    sendExecutionResult(ctx, null,
        stdout[workerId] ? [...stdout[workerId]] : [],
        "Execution timed out")
    .catch(e => console.log((new Date()).getTime(), ": ", e));
    delete stdout[workerId];
    console.log((new Date()).getTime(), ": ", stdout);
  }, body.maxExecutionTime ? body.maxExecutionTime : 30000);
}

function setIntervalHandler(worker, ctx, body) {
  const workerId = getWorkerId(body, null, null)
  return setInterval(() => {
    // Terminate the Web Worker if it has exceeded its cpu or memory thresholds
    if (threadStatus.has(workerId)) {
      const status: ThreadStatus = threadStatus.get(workerId)
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
        // console.log((new Date()).getTime(), ": ", "DEBUG 4: sendExecutionResult")
        sendExecutionResult(ctx, null,
            [...stdout[workerId]],
            errorMessage)
        .catch(e => console.log((new Date()).getTime(), ": ", e));
      }
    }
  }, 10);
}

function spawnNewIsolate(ctx, userScriptUrl, body) {
  // Create a new Web Worker
  const workerId = getWorkerId(body, null, null)
  const worker = new Worker(userScriptUrl,
      {
        type: "module",
        name: workerId
      }
  );
  workersPendingId.enqueue(workerId)
  const _threadStat: ThreadStatus = {
    updatedAt: -1,
    cpuTime: -1,
    memoryUsage: -1,
    cpuThreshold: body.maxCpuTime ? body.maxCpuTime : 10,
    memoryThreshold: body.maxMemoryUsage ? body.maxMemoryUsage : 134217728,
    cpuBaseLine: -1
  }
  const timeoutId = setTimeoutHandler(worker, ctx, body)
  const intervalId = setIntervalHandler(worker, ctx, body)
  threadStatus.set(workerId, _threadStat)
  registerEventListener(worker, ctx, timeoutId, intervalId);

  // let timeoutId, interval;
  // // Listen for messages from the Web Worker
  // worker.onmessage = (event) => {
  //   const uid = event.data.uid;
  //   if (event.data.stdout) {
  //     let prevStdout = stdout[uid];
  //     if (!prevStdout) {
  //       prevStdout = []
  //     }
  //     prevStdout.push(event.data.stdout);
  //     stdout[uid] = prevStdout;
  //   } else if (event.data.result) {
  //     if (timeoutId) clearTimeout(timeoutId);
  //     if (interval) clearInterval(interval)
  //     workerStartTimes.delete(event.data.uid)
  //     console.log((new Date()).getTime(), ": ", "DEBUG 0: sendExecutionResult")
  //     sendExecutionResult(ctx,
  //         event.data.result,
  //         [...stdout[uid]],
  //         null)
  //     .catch(e => console.log((new Date()).getTime(), ": ", e))
  //     stdout[uid] = [];
  //   } else if (event.data.error) {
  //     worker.terminate();
  //     if (timeoutId) clearTimeout(timeoutId);
  //     if (interval) clearInterval(interval)
  //     workerStartTimes.delete(event.data.uid)
  //     console.log((new Date()).getTime(), ": ", "DEBUG 1: sendExecutionResult")
  //     sendExecutionResult(ctx,
  //         null,
  //         [...stdout[uid]],
  //         event.data.error)
  //     .catch(e => console.log((new Date()).getTime(), ": ", e))
  //     stdout[uid] = [];
  //   } else if (event.data.startedAt) {
  //     console.log((new Date()).getTime(), ": ", "Setting start time: ", event.data.uid, event.data.startedAt)
  //     workerStartTimes.set(event.data.uid, event.data.startedAt)
  //   }
  // };

  // Handle errors from the Web Worker
  registerErrorListener(worker, ctx, body, timeoutId, intervalId);
  // worker.onerror = (error) => {
  //   error.preventDefault();
  //   if (timeoutId) clearTimeout(timeoutId);
  //   if (interval) clearInterval(interval)
  //   console.log((new Date()).getTime(), ": ", "DEBUG 2: sendExecutionResult")
  //   sendExecutionResult(ctx, null,
  //       [...stdout[body.uid]],
  //       error.message)
  //   .catch(e => console.log((new Date()).getTime(), ": ", e));
  //   stdout[body.uid] = [];
  // };

  stdout[workerId] = [];
  worker.postMessage({uid: body.uid, payload: body.payload, execId: body.execId});
  console.log((new Date()).getTime(), ": ", "workerId: ", workerId, " Request received at: ", (new Date()).getTime());

  // setTimeoutHandler(worker, ctx, body)
  // timeoutId = setTimeout(() => {
  //   // Terminate the Web Worker when the timeout occurs
  //   worker.terminate();
  //   console.log((new Date()).getTime(), ": ", "DEBUG 3: sendExecutionResult")
  //   sendExecutionResult(ctx, null,
  //       stdout[body.uid] ? [...stdout[body.uid]] : [],
  //       "Execution timed out")
  //   .catch(e => console.log((new Date()).getTime(), ": ", e));
  //   delete stdout[body.uid];
  //   console.log((new Date()).getTime(), ": ", stdout);
  // }, body.maxExecutionTime ? body.maxExecutionTime : 30000);

  // interval = setInterval(() => {
  //   // Terminate the Web Worker if it has exceeded its cpu or memory thresholds
  //   if (threadStatus.has(body.uid)) {
  //     const status: ThreadStatus = threadStatus.get(body.uid)
  //     let errorMessage = undefined
  //     if (status?.cpuTime > status?.cpuBaseLine) {
  //       const delta = status.cpuTime - status.cpuBaseLine
  //       if (delta > status.cpuThreshold) {
  //         errorMessage = "CPU timeout"
  //       }
  //     } else if (status?.memoryUsage > status?.memoryThreshold) {
  //       // TODO: consider setting a memory threshold, e.g. what if the initial usage ends up
  //       //  being greater than the threshold even before the user code could run?
  //       errorMessage = "Out of memory"
  //     }
  //     if (errorMessage) {
  //       worker.terminate();
  //       console.log((new Date()).getTime(), ": ", "DEBUG 4: sendExecutionResult")
  //       sendExecutionResult(ctx, null,
  //           [...stdout[body.uid]],
  //           errorMessage)
  //       .catch(e => console.log((new Date()).getTime(), ": ", e));
  //       workerStartTimes.delete(body.uid)
  //       threadStatus.delete(body.uid)
  //     }
  //   }
  // }, 10);
}


async function sendExecutionResult(ctx, result, stdout, error) {
  const body = await getBody(ctx);
  if (body.uid && body.execId) {
    const workerId = getWorkerId(body, null, null)
    clearWorkerId(workerId)
    exitedWorkersByWorkerId.set(workerId, true)
    console.log((new Date()).getTime(), ": ", "exitedWorkersByWorkerId size: ", exitedWorkersByWorkerId.size);
    console.log((new Date()).getTime(), ": ", "EXITING for: ", workerId)
    console.log((new Date()).getTime(), ": ", "sendExecutionResult - activeWorkersByThreadId size: ", activeWorkersByThreadId.size)
    console.log((new Date()).getTime(), ": ", "sendExecutionResult - activeWorkersByWorkerId size: ", activeWorkersByWorkerId.size)
    console.log((new Date()).getTime(), ": ", "sendExecutionResult - threadStatus size: ", threadStatus.size)
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
    console.log((new Date()).getTime(), ": ", "SendingResult: body ", body)
    console.log((new Date()).getTime(), ": ", "SendingResult data: ", data)
    const sendStatus = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + body.apiKey
      },
      body: JSON.stringify(data),
    });
    const rt = ctx.response.headers.get("X-Response-Time");
    console.log((new Date()).getTime(), ": ", `${ctx.request.method} - Egress - ${url} - ${sendStatus.status} - ${rt}ms`);
  }
}

async function executeUserCode(ctx) {
  const body = await getBody(ctx);
  const host = getHost(body.env);
  const url = `${host}/npm/${body.uid}`;

  // Synchronize any concurrent requests here for proper attribution of thread ID from the
  // thread monitor to the correct worker. The workersPendingId queue needs to be empty before
  // we can schedule another task.
  let synchronizationInterval = undefined
  synchronizationInterval = setInterval(() => {
    if (workersPendingId.size() === 0) {
      console.log((new Date()).getTime(), ": ", "Spawning new worker: ", getWorkerId(body))
      if (synchronizationInterval) {
        clearInterval(synchronizationInterval)
      }
      spawnNewIsolate(ctx, url, body);
    } else {
      console.log((new Date()).getTime(), ": ", "Waiting to synchronize: ", workersPendingId.peek())
    }
  }, 5)
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
  const workerId = workersPendingId.peek()
  workersPendingId.dequeue();
  // Due to race conditions, a worker may have finished already by the time we
  // receive a new thread signal. This is a side effect of scanning for new threads at
  // a 5-millisecond interval and the actual execution of user code being faster than
  // the interval for non-blocking code.
  console.log((new Date()).getTime(), ": ", "handleNewThreadSignal exitedWorkersByWorkerId size: ", exitedWorkersByWorkerId.size)
  console.log((new Date()).getTime(), ": ", "handleNewThreadSignal TID: ", body.tid)
  if (!exitedWorkersByWorkerId.has(workerId)) {
    console.log((new Date()).getTime(), ": ", "Setting thread id: ", body.thread_id, "  workerId: ", workerId)
    activeWorkersByThreadId.set(body.thread_id, workerId);
    activeWorkersByWorkerId.set(workerId, body.thread_id)
    console.log((new Date()).getTime(), ": ", "Added new worker to activeWorkersByThreadId");
    console.log((new Date()).getTime(), ": ", "pending worker size: ", workersPendingId.size());
    console.log((new Date()).getTime(), ": ", "active worker size: ", activeWorkersByThreadId.size)
  } else {
    console.log((new Date()).getTime(), ": ", "EXITED handleNewThreadSignal workerId, TID: ", workerId, body.tid)
  }
  ctx.response.body = {status: "ok"} ;
}

async function handleThreadAlarm(ctx) {
  const body = await getBody(ctx);
  // TODO: Remove thread_id from activeWorkersByThreadId if the worker has finished
  console.log((new Date()).getTime(), ": ", "activeWorkersByThreadId: ", activeWorkersByThreadId.size);
  const workerId = activeWorkersByThreadId.get(body.thread_id)
  if (workerId) {
    console.log((new Date()).getTime(), ": ", "handleThreadAlarm workerId, TID: ", workerId, body.tid)
    console.log((new Date()).getTime(), ": ", "Worker found...")
    // It takes about 2 seconds to teardown a worker. That is too expensive of an operation.
    // Need to improve that!
    const newUpdatedAt = body.updated_at
    // WorkerStartTime could be undefined until the worker has fully initialized. There is a 300-400
    // millisecond latency due to setting up the environment with npm imports, etc.
    // This should be dealt with later. The simplest function with just a console log takes
    // 60 milliseconds end-to-end (security validation, entitlement check from the db, and others).
    // It's worth investigating what the baseline time is without any of those overheads.
    const workerStartTime = workerStartTimes.get(workerId)
    console.log((new Date()).getTime(), ": ", "workerStartTime: ", workerStartTime)
    console.log((new Date()).getTime(), ": ", "newUpdatedAt: ", newUpdatedAt)
    if (workerStartTime && newUpdatedAt >= workerStartTime) {
      console.log((new Date()).getTime(), ": ", "Updating status for: ", workerId)
      const prevStatus: ThreadStatus = threadStatus.get(workerId);
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
      threadStatus.set(workerId, updatedStatus)
    }
  }
  ctx.response.body = {status: "ok"} ;
}

const app = new Application();

// Logger
app.use(async (ctx, next) => {
    await next();
    const rt = ctx.response.headers.get("X-Response-Time");
    console.log((new Date()).getTime(), ": ", `${ctx.request.method} - Ingress - ${ctx.request.url} - ${rt}`);
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
      console.log((new Date()).getTime(), ": ", "DEBUG 5 error: ", error.message)
      console.log((new Date()).getTime(), ": ", "DEBUG 5: sendExecutionResult")
      await sendExecutionResult(context, null, null, error.message);
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

console.log((new Date()).getTime(), ": ", "Server listening on port 8000");
await app.listen({ port: 8000 });
