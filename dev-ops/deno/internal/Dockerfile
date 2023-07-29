FROM denoland/deno:alpine
COPY main.ts main.ts
USER deno
RUN deno cache main.ts
RUN mkdir -p /var/tmp/log
EXPOSE 9000
CMD ["run", "--allow-all", "main.ts"]