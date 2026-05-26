FROM eclipse-temurin:21-jre-alpine

ARG OTEL_AGENT_VERSION=2.28.1
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar \
    /app/opentelemetry-javaagent.jar

WORKDIR /app
COPY target/demo-1.0.0.jar app.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar"
ENV OTEL_SERVICE_NAME=demo
ENV OTEL_TRACES_EXPORTER=otlp
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://172.17.0.1:4317
ENV OTEL_EXPORTER_OTLP_PROTOCOL=grpc
ENV OTEL_LOGS_EXPORTER=none
ENV OTEL_METRICS_EXPORTER=none

ENTRYPOINT ["java", "-jar", "app.jar"]
