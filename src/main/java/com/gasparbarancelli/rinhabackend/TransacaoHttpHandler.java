package com.gasparbarancelli.rinhabackend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.postgresql.util.PSQLException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

final class TransacaoHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var customExchange = new CustomHttpExchange(exchange);

        var path = customExchange.getPath();
        var split = path.split("/");
        var id = Integer.parseInt(split[2]);

        if (Cliente.naoExiste(id)) {
            exchange.sendResponseHeaders(404, 0);
            exchange.close();
            return;
        }

        if (customExchange.isPost()) {
            doPost(customExchange, id);
        } else if (customExchange.isGet()) {
            doGet(customExchange, id);
        }
    }

    private void doPost(CustomHttpExchange exchange, int clienteId) {
        try {
            var body = exchange.getBody();
            var transacaoRequisicao = TransacaoMapper.map(body);
            DataSource.insert(transacaoRequisicao.geraTransacao(clienteId));
            exchange.sendResponseHeaders(200, 0);
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, 0);
        } catch (Exception e) {
            exchange.sendResponseHeaders(422, 0);
        } finally {
            exchange.close();
        }
    }

    private void doGet(CustomHttpExchange exchange, int id) {
        try {
            var optionalJson = DataSource.findById(id)
                    .map(TransacaoMapper::map);

            if (optionalJson.isPresent()) {
                var json = optionalJson.get();
                exchange.addHeader("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, json.length());
                exchange.setBody(json);
            } else {
                exchange.sendResponseHeaders(404, 0);
            }
        } finally {
            exchange.close();
        }
    }

}
