package com.gasparbarancelli.rinhabackend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

final class ContagemPessoasHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var count = String.valueOf(PessoaDataSource.count());
        var customExchange = new CustomHttpExchange(exchange);
        customExchange.sendResponseHeaders(200, count.length());
        customExchange.setBody(count);
        customExchange.close();
    }

}
