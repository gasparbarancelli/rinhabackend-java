package com.gasparbarancelli.rinhabackend;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

final class CustomHttpExchange {

    private static final Logger LOGGER = Logger.getLogger(CustomHttpExchange.class.getName());

    private final HttpExchange exchange;

    public CustomHttpExchange(HttpExchange exchange) {
        this.exchange = exchange;
    }

    boolean isPost() {
        var method = exchange.getRequestMethod();
        return "POST".equals(method);
    }

    boolean isGet() {
        var method = exchange.getRequestMethod();
        return "GET".equals(method);
    }

    void sendResponseHeaders(int rCode, long responseLength) {
        try {
            exchange.sendResponseHeaders(rCode, responseLength);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    String getPath() {
        return exchange.getRequestURI().getPath();
    }

    void addHeader(String key, String value) {
        exchange.getResponseHeaders().add(key, value);
    }

    void close() {
        try {
            exchange.getResponseBody().close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    String getBody() {
        try {
            return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            sendResponseHeaders(400, 0);
            throw new RuntimeException(e);
        }
    }

    void setBody(String body) {
        try {
            OutputStream os = exchange.getResponseBody();
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            sendResponseHeaders(500, 0);
        }
    }

}
