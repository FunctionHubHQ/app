import { Application, isHttpError } from "https://deno.land/x/oak/mod.ts";

import {
  getTypeScriptReader,
  getOpenApiWriter,
  makeConverter,
  getOpenApiReader,
  getTypeScriptWriter
} from 'npm:typeconv';

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
    uid: body.uid,
    error: error,
    result: result
  }
  console.log("About to send data: ", JSON.stringify(data));
  console.log("url: ", getHost(body.env) + "/s-result");
  const x = await fetch(getHost(body.env) + "/s-result", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
  console.log("Server response: ", x);
  ctx.response.body = {status: "ok"};
}

async function generateOpenApiSpec(ctx) {
  const body = await getBody(ctx);
  const reader = getTypeScriptReader( );
  const writer = getOpenApiWriter( {
    format: 'json',
    title: 'GPT Lambda API',
    version: 'v1'
  } );
  const { convert } = makeConverter( reader, writer );
  const { data } = await convert( { data: body.file } );
  await sendResult(ctx, {
    spec: data,
    format: "json"
  }, null);
  ctx.response.body = data
}

async function generateTypeScript(ctx) {
  const body = await getBody(ctx);
  const reader = getOpenApiReader( );
  const writer = getTypeScriptWriter( {
    format: 'ts',
    title: 'GPT Lambda API',
    version: 'v1'
  } );
  const { convert } = makeConverter( reader, writer );
  const { data } = await convert( { data: body.file } );
  await sendResult(ctx, {
    spec: data,
    format: "ts"
  }, null);
  ctx.response.body = data
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
    if (path.includes("/code-gen/ts-to-oa") ) {
      await generateOpenApiSpec(ctx);
    } else if (path.includes("/code-gen/oa-to-ts") ) {
      await generateTypeScript(ctx);
    } else {
        ctx.response.body = {status: "Deno Internal Server is healthy"} ;
    }
  });

console.log("Server listening on port 9000");
await app.listen({ port: 9000 });
