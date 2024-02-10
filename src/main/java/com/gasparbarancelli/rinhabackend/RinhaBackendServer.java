package com.gasparbarancelli.rinhabackend;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class RinhaBackendServer {

    private static final Logger LOGGER = Logger.getLogger(RinhaBackendServer.class.getName());

    public static void main(String[] args) throws IOException {
        LOGGER.info("Iniciando aplicacao");
        new RinhaBackendServer().startServer();
    }

    public void startServer() throws IOException {
        var httpServer = HttpServer.create(httpPort(), 0);
        httpServer.createContext("/clientes", new TransacaoHttpHandler());
        httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        httpServer.start();
    }

    private InetSocketAddress httpPort() {
        var port = Optional.ofNullable(System.getenv("HTTP_PORT"))
                .map(Integer::valueOf)
                .orElse(8080);

        LOGGER.info("Servidor http respondendo na porta " + port);

        return new InetSocketAddress(port);
    }

}
