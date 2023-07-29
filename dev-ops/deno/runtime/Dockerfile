FROM denoland/deno:alpine
COPY main.ts main.ts
USER deno
RUN deno cache main.ts
RUN mkdir -p /var/tmp/log
EXPOSE 8000
CMD ["run", "--allow-net", "main.ts"]