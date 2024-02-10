package com.gasparbarancelli.rinhabackend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

final class TransacaoHttpHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(TransacaoHttpHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var customExchange = new CustomHttpExchange(exchange);

        var path = customExchange.getPath();
        LOGGER.log(Level.INFO, path);

        var split = path.split("/");
        var id = Integer.parseInt(split[2]);
        LOGGER.log(Level.INFO, "Codigo do cliente: " + id);

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
            LOGGER.log(Level.INFO, body);
            var transacaoRequisicao = TransacaoMapper.map(body);
            var transacaoResposta = DataSource.insert(transacaoRequisicao.geraTransacao(clienteId));

            var json = TransacaoMapper.map(transacaoResposta);

            exchange.addHeader("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.length());
            exchange.setBody(json);
        } catch (SQLException e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, 0);
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(422, 0);
        } finally {
            exchange.close();
        }
    }

    private void doGet(CustomHttpExchange exchange, int clienteId) {
        try {
            var extrato = DataSource.extrato(clienteId);
            var json = TransacaoMapper.map(extrato);

            exchange.addHeader("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.length());
            exchange.setBody(json);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }

}
