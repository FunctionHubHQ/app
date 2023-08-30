docker run --name mitmproxy --rm -d -p 7070:8080 -p 7071:8081 --add-host=host.docker.internal:host-gateway -v $(pwd):/data  -v ~/.mitmproxy:/home/mitmproxy/.mitmproxy mitmproxy/mitmproxy mitmweb --web-host 0.0.0.0 --web-port 8081 -s /data/scripts/redirect.py -s /data/scripts/modify_response.py
docker logs mitmproxy -f
