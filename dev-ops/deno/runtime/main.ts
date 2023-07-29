import { Application, isHttpError } from "https://deno.land/x/oak/mod.ts";


const devHost = 'http://host.docker.internal:8080';
const prodHost = 'http://api:8080';

/**
 * Keeps track of console logs from user scripts workers
 */
const stdout = {}

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
//
// const timeoutPromise = new Promise((_resolve, reject) => {
//   setTimeout(
//       () => reject(new Error('Async call timeout limit reached')),
//       time
//   );
// });

// const timeout = (userPromise, time) =>
//     Promise.race([userPromise,
//       new Promise((_r, rej) => setTimeout(rej, time))]);

// const timeout = (userPromise, time) =>
//     Promise.race([userPromise,
//       new Promise((_r, rej) => setTimeout(
//           () => rej(new Error('Async call timeout limit reached')),
//           time
//       ))
//     ]);

// /**
//  * Call an async function with a maximum time limit (in milliseconds) for the timeout
//  * @param {Promise<any>} asyncPromise An asynchronous promise to resolve
//  * @param {number} timeLimit Time limit to attempt function in milliseconds
//  * @returns {Promise<any> | undefined} Resolved promise for async function call, or an error if time limit reached
//  * Credit: https://javascript.plainenglish.io/how-to-add-a-timeout-limit-to-asynchronous-javascript-functions-3676d89c186d
//  */
// const asyncCallWithTimeout = async (asyncPromise, timeLimit) => {
//   let timeoutHandle;
//
//   const timeoutPromise = new Promise((_resolve, reject) => {
//     timeoutHandle = setTimeout(
//         () => reject(new Error('Async call timeout limit reached')),
//         timeLimit
//     );
//   });
//
//   return Promise.race([asyncPromise, timeoutPromise]).then(result => {
//     clearTimeout(timeoutHandle);
//     return result;
//   })
// }


function executeUserScript(ctx, userScriptUrl, body) {
  // Create a new Web Worker
  const worker = new Worker(userScriptUrl,
      {
        type: "module"
      }
  );

  let timeoutId;
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
      clearTimeout(timeoutId);
      sendResult(ctx,
          event.data.result,
          [...stdout[uid]],
          null)
      .catch(e => console.log(e))
      stdout[uid] = [];
    } else if (event.data.error) {
      worker.terminate();
      console.log("END TIME: ", Date.now()/1000);
      clearTimeout(timeoutId);
      sendResult(ctx,
          null,
          [...stdout[uid]],
          event.data.error)
      .catch(e => console.log(e))
      stdout[uid] = [];
    }
  };

  // Handle errors from the Web Worker
  worker.onerror = (error) => {
    clearTimeout(timeoutId);
    console.error('Error in the user script:', error.message);
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
    stdout[body.uid] = [];
    console.log(stdout);
  }, body.timeout);
}


async function sendResult(ctx, result, stdout, error) {
  const body = await getBody(ctx);
  const data = {
    fcm_token: body.fcmToken,
    uid: body.uid,
    error: error,
    result: result,
    std_out: stdout,
  }
  await fetch(getHost(body.env) + "/e-result", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
}

async function executeUserCode(ctx) {
  const body = await getBody(ctx);
  // const timeoutFunc = fetch('https://baconipsum.com/api/?type=meat-and-filler');
  const host = getHost(body.env);
  // const module = await import(`${host}/npm/${body.uid}.ts`);
  const url = `${host}/npm/${body.uid}.ts`;
  console.log("DEBUG 1 - calling executeUserScript: ", url);
  executeUserScript(ctx, url, body);
}

const app = new Application();

// Logger
app.use(async (ctx, next) => {
    await next();
    const rt = ctx.response.headers.get("X-Response-Time");
    console.log(`${ctx.request.method} ${ctx.request.url} - ${rt}`);
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
    } else {
        ctx.response.body = {status: "GPT Lambda is healthy"} ;
    }
  });

console.log("Server listening on port 8000");
await app.listen({ port: 8000 });
