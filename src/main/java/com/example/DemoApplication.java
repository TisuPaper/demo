package com.example;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
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

        // ── PARENT span ────────────────────────────────────────────────────
        Span parent = tracer.spanBuilder("processRequest")
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute("request.type", "hello")
            .startSpan();

        try (Scope parentScope = parent.makeCurrent()) {

            // parent is now current on this thread
            // all child spans below auto-link to it

            validate();    // child 1
            compute();     // child 2
            persist();     // child 3

            parent.addEvent("all.steps.completed");
            parent.setStatus(StatusCode.OK);
            return "Hello from OTel!";

        } catch (Exception e) {
            parent.recordException(e);
            parent.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;

        } finally {
            parent.end();
        }
    }

    // ── CHILD 1 ────────────────────────────────────────────────────────────
    private void validate() {
        Span span = tracer.spanBuilder("validate")
            .setParent(Context.current())       // explicit parent link
            .setAttribute("step", "1")
            .startSpan();

        try (Scope s = span.makeCurrent()) {
            Thread.sleep(30);                   // simulate work
            span.addEvent("validation.passed");

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw new RuntimeException(e);

        } finally {
            span.end();
        }
    }

    // ── CHILD 2 ────────────────────────────────────────────────────────────
    private void compute() {
        Span span = tracer.spanBuilder("compute")
            .setParent(Context.current())
            .setAttribute("step", "2")
            .startSpan();

        try (Scope s = span.makeCurrent()) {
            Thread.sleep(50);                   // simulate work
            span.setAttribute("compute.result", 42);

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw new RuntimeException(e);

        } finally {
            span.end();
        }
    }

    // ── CHILD 3 ────────────────────────────────────────────────────────────
    private void persist() {
        Span span = tracer.spanBuilder("persist")
            .setParent(Context.current())
            .setAttribute("step", "3")
            .setAttribute("db.system", "simulated")
            .startSpan();

        try (Scope s = span.makeCurrent()) {
            Thread.sleep(20);                   // simulate work
            span.addEvent("record.saved");

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw new RuntimeException(e);

        } finally {
            span.end();
        }
    }
}
