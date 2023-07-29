import { Application, isHttpError } from "https://deno.land/x/oak/mod.ts";


const devHost = 'http://host.docker.internal:8080';
const prodHost = 'http://api:8080';

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

async function sendResult(ctx, result, error) {
  const body = await getBody(ctx);
  const data = {
    fcm_token: body.fcmToken,
    uid: body.uid,
    error: error,
    result: result
  }
  await fetch(getHost(body.env) + "/e-result", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
  ctx.response.body = {status: "ok"};
}

async function executeUserCode(ctx) {
  const body = await getBody(ctx);
  const host = getHost(body.env);
  const module = await import(`${host}/npm/${body.uid}.ts`);
  const result = await module.default(body.payload);
  await sendResult(ctx, result, null);
  ctx.response.body = {status: "ok"}
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
      await sendResult(context, null, error.message);
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
