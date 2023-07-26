import { Application, isHttpError } from "https://deno.land/x/oak/mod.ts";

const devHost = 'http://host.docker.internal:8080';
const prodHost = 'http://api:8080/';
async function sendResult(ctx, result, error) {
  const body = JSON.parse(await ctx.request.body().value);
  const environment = body.env
  let resultCallBackUrl;
  if (environment === "prod") {
    resultCallBackUrl = `${prodHost}/result`;
  } else {
    resultCallBackUrl = `${devHost}/result`;
  }
  const data = {
    fcm_token: body.fcmToken,
    hash: body.hash,
    error: error,
    result: result
  }
  const x = await fetch(resultCallBackUrl, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
  ctx.response.body = {status: "ok"};
}

async function executeUserCode(ctx) {
  const body = JSON.parse(await ctx.request.body().value);
  const environment = body.env
  let module
  if (environment === "prod") {
    module = await import(`${prodHost}/npm/${body.hash}.ts`);
  } else {
    module = await import(`${devHost}/npm/${body.hash}.ts`);
  }
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
