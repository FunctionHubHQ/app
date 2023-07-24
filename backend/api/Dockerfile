FROM amazoncorretto:20.0.1-al2-generic
ENV SPRING_APPLICATION_NAME=shoppiem
ENV SPRING_PROFILES_ACTIVE=test
ENV MR_GH_USERNAME=bmelesse
ENV MR_GH_TOKEN=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
EXPOSE 8080
COPY build/libs/app.jar /app.jar
ENTRYPOINT ["java","-jar", "--enable-preview", "/app.jar"]
