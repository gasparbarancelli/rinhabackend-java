package com.gasparbarancelli.rinhabackend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.postgresql.util.PSQLException;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.UUID;

final class PessoasHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var customExchange = new CustomHttpExchange(exchange);
        if (customExchange.isPost()) {
            doPost(customExchange);
        } else if (customExchange.isGet()) {
            doGet(customExchange);
        }
    }

    private void doPost(CustomHttpExchange exchange) {
        try {
            String body = exchange.getBody();
            Pessoa pessoa = PessoaMapper.map(body);
            var id = PessoaDataSource.insert(pessoa);
            exchange.addHeader("Location", " /pessoas/" + id);
            exchange.sendResponseHeaders(201, 0);
        } catch (PSQLException e) {
            exchange.sendResponseHeaders(422, 0);
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, 0);
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, 0);
        } finally {
            exchange.close();
        }
    }

    private void doGet(CustomHttpExchange exchange) {
        try {
            var path = exchange.getPath();
            var split = path.split("/");
            if (split.length == 3) {
                var id = UUID.fromString(split[2]);
                findById(id, exchange);
            } else {
                findByTerm(exchange);
            }
        } finally {
            exchange.close();
        }
    }

    private void findByTerm(CustomHttpExchange exchange) {
        var query = exchange.getQuery();
        if (query == null || query.isBlank() || !query.trim().startsWith("t=") || query.split("=").length == 1) {
            exchange.sendResponseHeaders(400, 0);
        } else {
            var search = query.split("=")[1];
            var pessoas = PessoaDataSource.findByTerm(search);
            var json = PessoaMapper.map(pessoas);
            exchange.addHeader("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.length());
            exchange.setBody(json);
        }
    }

    private void findById(UUID id, CustomHttpExchange exchange) {
        var optionalJson = PessoaDataSource.findById(id)
                .map(PessoaMapper::map);

        if (optionalJson.isPresent()) {
            var json = optionalJson.get();
            exchange.addHeader("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.length());
            exchange.setBody(json);
        } else {
            exchange.sendResponseHeaders(404, 0);
        }
    }

}
