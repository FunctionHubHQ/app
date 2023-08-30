"""
This example shows two ways to redirect flows to another server.
"""
from mitmproxy import http

def request(flow: http.HTTPFlow) -> None:
    # pretty_host takes the "Host" header of the request into account,
    # which is useful in transparent mode where we usually only have the IP
    # otherwise.
#     if flow.request.pretty_host == "api.example.com":
      print("Attempting to redirect request...", flow.request.host)
      flow.request.headers["X-FH-Target"] = flow.request.url
      flow.request.host = "host.docker.internal"
      flow.request.port = 9090
      flow.request.scheme = "http" # Terminate SSL here


