package com.gasparbarancelli.rinhabackend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

final class TransacaoHttpHandler implements HttpHandler {

    private final DataSource dataSource = new DataSource();

    @Override
    public void handle(HttpExchange exchange) {
        var customExchange = new CustomHttpExchange(exchange);

        var path = customExchange.getPath();

        var split = path.split("/");
        var id = Integer.parseInt(split[2]);

        if (Cliente.naoExiste(id)) {
            customExchange.sendResponseHeaders(404, 0);
            return;
        }

        if (customExchange.isPost()) {
            doPost(customExchange, id);
            return;
        }

        doGet(customExchange, id);
    }

    private void doPost(CustomHttpExchange exchange, int clienteId) {
        try {
            var body = exchange.getBody();
            var transacaoRequisicao = TransacaoMapper.map(body);
            var transacaoResposta = dataSource.insert(transacaoRequisicao.geraTransacao(clienteId));

            var json = TransacaoMapper.map(transacaoResposta);
            exchange.sendResponseHeaders(200, json.length());
            exchange.setBody(json);
        } catch (Exception e) {
            exchange.sendResponseHeaders(422, 0);
        }
    }

    private void doGet(CustomHttpExchange exchange, int clienteId) {
        var extrato = dataSource.extrato(clienteId);
        var json = TransacaoMapper.map(extrato);
        exchange.sendResponseHeaders(200, json.length());
        exchange.setBody(json);
    }

}
