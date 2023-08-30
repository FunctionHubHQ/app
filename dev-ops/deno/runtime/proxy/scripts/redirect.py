"""
Terminate all incoming request SSLs here, add header, and redirect
to the api server.
"""
from mitmproxy import http

def request(flow: http.HTTPFlow) -> None:
    # pretty_host takes the "Host" header of the request into account,
    # which is useful in transparent mode where we usually only have the IP
    # otherwise.
    flow.request.headers["X-Function-Hub-Proxy-Target"] = flow.request.url
    flow.request.host = "host.docker.internal"
    flow.request.port = 9090
    flow.request.scheme = "http" # Terminate SSL here


