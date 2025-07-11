import { Application, isHttpError } from "https://deno.land/x/oak/mod.ts";

import {
  getTypeScriptReader,
  getOpenApiWriter,
  makeConverter,
  getOpenApiReader,
  getTypeScriptWriter,
  getJsonSchemaReader,
  getJsonSchemaWriter
} from 'npm:typeconv';

const devHost = 'http://host.docker.internal:9090';
const prodHost = 'http://api:9090';

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

async function sendResult(ctx, spec, error) {
  const body = await getBody(ctx);
  const data = {
    code_id: body.codeId,
    error: error,
    spec
  }
  const response = await fetch(getHost(body.env) + "/s-result", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": "Bearer " + body.apiKey
    },
    body: JSON.stringify(data),
  });
}

async function generateSchema(ctx, from, to) {
  const body = await getBody(ctx);
  let reader;
  let writer;
  if (from === "ts") {
    reader = getTypeScriptReader();
  } else if (from === "oapi") {
    reader = getOpenApiReader();
  } else if (from === "jsc") {
    reader = getJsonSchemaReader();
  }

  if (to === "ts") {
    writer = writer = getTypeScriptWriter( {
      format: 'ts',
      title: 'FunctionHub API',
      version: 'v1'
    } );
  } else if (to === "jsc") {
    writer = getJsonSchemaWriter();
  } else if (to === "oapi") {
    writer = getOpenApiWriter( {
      format: 'json',
      title: 'FunctionHub API',
      version: 'v1'
    } );
  }

  if (reader && writer) {
    const { convert } = makeConverter( reader, writer );
    const { data } = await convert( { data: body.file } );
    await sendResult(ctx,
        {
          value: data,
          format: "json"
        }, null);
    ctx.response.body = data
  }

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
    if (path.includes("/spec") ) {
      const body = await getBody(ctx);
      await generateSchema(ctx, body.from, body.to);
    } else {
        ctx.response.body = {status: "FunctionHub Spec Server is healthy"} ;
    }
  });

console.log("Server listening on port 9000");
await app.listen({ port: 9000 });
