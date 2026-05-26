package com.example;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Scope;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    private static final Tracer tracer =
        GlobalOpenTelemetry.getTracer("com.example.demo", "1.0.0");

    @GetMapping("/hello")
    public String hello() {

        Span span = tracer.spanBuilder("doWork")
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute("demo.key", "hello-world")
            .startSpan();

        try (Scope s = span.makeCurrent()) {
            Thread.sleep(100); // simulate work
            return "Hello from OTel!";
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw new RuntimeException(e);
        } finally {
            span.end();
        }
    }
}
