package com.gasparbarancelli.rinhabackend;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
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
        var socketAddress = getSocketAddress();
        if (socketAddress.isEmpty()) {
            return;
        }

        var httpServer = HttpServer.create(socketAddress.get(), 0);
        httpServer.createContext("/clientes", new TransacaoHttpHandler());
        httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        httpServer.start();
    }

    private Optional<InetSocketAddress> getSocketAddress() {
        var port = System.getenv("HTTP_PORT");
        if (Objects.isNull(port)) {
            return Optional.empty();
        }

        LOGGER.info("Servidor http respondendo na porta " + port);

        var socketAddress = new InetSocketAddress(Integer.parseInt(port));
        return Optional.of(socketAddress);
    }

}
