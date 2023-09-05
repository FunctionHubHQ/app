FROM denoland/deno:alpine
RUN apk add --no-cache python3 py3-pip
RUN apk update && apk add --no-cache supervisor openssh nginx
COPY main.ts main.ts
COPY utils/thread_monitor.py /
COPY supervisord.conf /etc/supervisord.conf
RUN deno cache main.ts
RUN mkdir -p /var/tmp/log
EXPOSE 8000
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]


#
#USER deno
#CMD ["run", "--allow-net", "--allow-env=NODE_EXTRA_CA_CERTS", "--allow-read=.mitmproxy/mitmproxy-ca.pem", "main.ts"]