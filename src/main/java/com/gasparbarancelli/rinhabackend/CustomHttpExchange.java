package com.gasparbarancelli.rinhabackend;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

final class CustomHttpExchange {

    private final HttpExchange exchange;

    public CustomHttpExchange(HttpExchange exchange) {
        this.exchange = exchange;
        exchange.getResponseHeaders().add("Content-Type", "application/json");
    }

    boolean isPost() {
        var method = exchange.getRequestMethod();
        return "POST".equals(method);
    }

    void sendResponseHeaders(int rCode, long responseLength) {
        try {
            exchange.sendResponseHeaders(rCode, responseLength);
        } catch (IOException ignore) {
        }
    }

    String getPath() {
        return exchange.getRequestURI().getPath();
    }

    String getBody() {
        try {
            return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            sendResponseHeaders(400, 0);
            throw new RuntimeException(e);
        }
    }

    void setBody(String body) {
        try {
            OutputStream os = exchange.getResponseBody();
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.close();
        } catch (IOException ignore) {
            sendResponseHeaders(500, 0);
        }
    }

}
