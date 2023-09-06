// Worker Boundary b7dbbfbb7723221173444ae44b734f5b5cbaa465b9cfa2a70c9819394acc1291
self.onmessage = async (event) => {
  const uid = event.data.uid;
  const execId = event.data.execId
  const accessToken = event.data.accessToken
  try {
    // Re-direct console.log statements
    // TODO: may need to handle other console calls
    self.console.log = (...args) => {
      const message = args.map(it => JSON.stringify(it)).join(' ');
      self.postMessage({ stdout: message, uid: uid });
    }
    self.process = {}
    self.process.env = {}
    const startedAt = (new Date()).getTime()
    // Log the time the user code has started running for accurate cpu usage
    // measurement attribution
    self.process.env["FH_ACCESS_TOKEN"] = accessToken;
    self.postMessage({startedAt: startedAt, uid: uid, execId: execId})
    const result = await handler(event.data.payload);
    self.postMessage({ result: result, uid: uid });
  } catch (e) {
    self.postMessage({ error: e.message, uid: uid });
  }
  self.close();
};