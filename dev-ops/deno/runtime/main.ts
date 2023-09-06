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

interface IntervalIds {
  intervalId?: number,
  timeoutId?: number
}

interface SynchronizationTimer {
  started_at?: number,
  updated_at?: number
}

const syncTimer: SynchronizationTimer = {
  started_at: 0,
  updated_at: 0
}

const MAX_SYNCHRONIZATION_WAIT_TIME = 1000
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
const workerIntervalIds = new Map<string, IntervalIds>();

function setCleanupInterval() {
  setInterval(() => {
    if (activeWorkersByWorkerId.size === 0 && activeWorkersByThreadId.size === 0) {
      exitedWorkersByWorkerId.clear()
    }
  }, 1000)
}

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

function clearTimeouts(workerId) {
  const ids: IntervalIds = workerIntervalIds.get(workerId)
  workerIntervalIds.delete(workerId)
  if (ids?.timeoutId) clearTimeout(ids.timeoutId);
  if (ids?.intervalId) clearInterval(ids.intervalId)
}

function clearWorkerId(workerId) {
  workerStartTimes.delete(workerId)
  threadStatus.delete(workerId)
  const threadId = activeWorkersByWorkerId.get(workerId)
  activeWorkersByWorkerId.delete(workerId)
  if (threadId) {
    activeWorkersByThreadId.delete(threadId)
  }
}

function registerEventListener(worker, ctx) {
  // Listen for messages from the Web Worker
  worker.onmessage = (event) => {
    const workerId = getWorkerId(null, event.data.uid, event.data.execId)
    if (event.data.stdout) {
      let prevStdout = stdout[workerId];
      if (!prevStdout) {
        prevStdout = []
      }
      prevStdout.push(event.data.stdout);
      stdout[workerId] = prevStdout;
    } else if (event.data.result) {
      clearTimeouts(workerId);
      sendExecutionResult(ctx,
          event.data.result,
          [...stdout[workerId]],
          null)
      .catch(e => console.error((new Date()).getTime(), ": ", e))
      stdout[workerId] = [];
    } else if (event.data.error) {
      worker.terminate();
      clearTimeouts(workerId);
      sendExecutionResult(ctx,
          null,
          [...stdout[workerId]],
          event.data.error)
      .catch(e => console.error((new Date()).getTime(), ": ", e))
      stdout[workerId] = [];
    } else if (event.data.startedAt) {
      workerStartTimes.set(workerId, event.data.startedAt)
    }
  };
}

function registerErrorListener(worker, ctx, body) {
  const workerId = getWorkerId(body, null, null)
  worker.onerror = (error) => {
    error.preventDefault();
    clearTimeouts(workerId);
    sendExecutionResult(ctx, null,
        [...stdout[workerId]],
        error.message)
    .catch(e => console.error((new Date()).getTime(), ": ", e));
    stdout[workerId] = [];
  };
}

function setTimeoutHandler(worker, ctx, body) {
  const workerId = getWorkerId(body, null, null)
  return setTimeout(() => {
    // Terminate the Web Worker when the timeout occurs
    worker.terminate();
    sendExecutionResult(ctx, null,
        stdout[workerId] ? [...stdout[workerId]] : [],
        "Execution timed out")
    .catch(e => console.error((new Date()).getTime(), ": ", e));
    delete stdout[workerId];
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
          errorMessage = `CPU timeout ${status.cpuTime}ms`
        }
      } else if (status?.memoryUsage > status?.memoryThreshold) {
        // TODO: consider setting a memory threshold, e.g. what if the initial usage ends up
        //  being greater than the threshold even before the user code could run?
        errorMessage = "Out of memory"
      }
      if (errorMessage) {
        worker.terminate();
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
  workerIntervalIds.set(workerId, {
    intervalId: intervalId,
    timeoutId: timeoutId
  })
  threadStatus.set(workerId, _threadStat)
  registerEventListener(worker, ctx);
  registerErrorListener(worker, ctx, body);

  stdout[workerId] = [];
  worker.postMessage({
    uid: body.uid,
    payload: body.payload,
    execId: body.execId,
    accessToken: body.accessToken
  });
}

async function sendExecutionResult(ctx, result, stdout, error) {
  const body = await getBody(ctx);
  if (body.uid && body.execId) {
    const workerId = getWorkerId(body, null, null)
    clearWorkerId(workerId)
    clearTimeouts(workerId)
    exitedWorkersByWorkerId.set(workerId, true)
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
    const elapsed = syncTimer.updated_at - syncTimer.started_at
    if (workersPendingId.size() === 0 || elapsed > MAX_SYNCHRONIZATION_WAIT_TIME) {
      // If synchronization attempt fails after 1 second, then go ahead and spawn the next
      // isolate. If we can't synchronize, then something must have happened to the thread
      // corresponding to the workerId at the head of the queue. This could mean the thread got
      // killed and so the thread monitor can no longer locate it.

      if (workersPendingId.size() > 0) {
      //   // We've exceeded MAX_SYNCHRONIZATION_WAIT_TIME so we won't be able to monitor this
      //   // dequeued worker. In all likelihood, the worker is also dead so this would be
      //   // removing the deadlock.
        workersPendingId.dequeue();
      }
      syncTimer.started_at = 0
      syncTimer.updated_at = 0
      if (synchronizationInterval) {
        clearInterval(synchronizationInterval)
      }
      spawnNewIsolate(ctx, url, body);
    } else {
      const now = (new Date()).getTime()
      syncTimer.started_at = syncTimer.started_at === 0 ? now :syncTimer.started_at
      syncTimer.updated_at = now
    }
  }, 10)
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
  if (!exitedWorkersByWorkerId.has(workerId)) {
    activeWorkersByThreadId.set(body.thread_id, workerId);
    activeWorkersByWorkerId.set(workerId, body.thread_id)
  }
  ctx.response.body = {status: "ok"} ;
}

async function handleThreadAlarm(ctx) {
  const body = await getBody(ctx);
  const workerId = activeWorkersByThreadId.get(body.thread_id)
  if (workerId) {
    // It takes about 2 seconds to teardown a worker. That is too expensive of an operation.
    // Need to improve that!
    const newUpdatedAt = body.updated_at
    // WorkerStartTime could be undefined until the worker has fully initialized. There is a 300-400
    // millisecond latency due to setting up the environment with npm imports, etc.
    // This should be dealt with later. The simplest function with just a console log takes
    // 60 milliseconds end-to-end (security validation, entitlement check from the db, and others).
    // It's worth investigating what the baseline time is without any of those overheads.
    const workerStartTime = workerStartTimes.get(workerId)
    if (workerStartTime && newUpdatedAt >= workerStartTime) {
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
    const url: string = ctx.request.url.pathname
    if (!url.includes("thread-alarm")) {
      console.log((new Date()).getTime(), ": ", `${ctx.request.method} - Ingress - ${ctx.request.url} - ${rt}`);
    }
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
setCleanupInterval()
await app.listen({ port: 8000 });
