package com.gasparbarancelli.rinhabackend;

import io.javalin.Javalin;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class RinhaBackendServer {

    private static final Logger LOGGER = Logger.getLogger(RinhaBackendServer.class.getName());

    private final DataSource dataSource = new DataSource();

    public static void main(String[] args) {
        LOGGER.info("Iniciando aplicacao");
        new RinhaBackendServer().startServer();
    }

    public void startServer() {
        var portNumber = getPortNumber();
        if (portNumber.isEmpty()) {
            LOGGER.info("Defina a variavel de ambiente HTTP_PORT para iniciar o servidor web");
            return;
        }

        Javalin.create(config -> {
            config.useVirtualThreads = true;
            config.http.asyncTimeout = 10_000L;
        }).get("/clientes/{clienteId}/extrato", ctx -> {
            var clienteId = Integer.parseInt(ctx.pathParam("clienteId"));
            if (Cliente.naoExiste(clienteId)) {
                ctx.status(404);
                return;
            }
            var extrato = dataSource.extrato(clienteId);
            var json = TransacaoMapper.map(extrato);
            ctx.result(json);
            ctx.status(200);
            ctx.contentType("application/json");
        }).post("/clientes/{clienteId}/transacoes", ctx -> {
            try {
                var clienteId = Integer.parseInt(ctx.pathParam("clienteId"));
                if (Cliente.naoExiste(clienteId)) {
                    ctx.status(404);
                    return;
                }
                var body = ctx.body();
                var transacaoRequisicao = TransacaoMapper.map(body);
                var transacaoResposta = dataSource.insert(transacaoRequisicao.geraTransacao(clienteId));

                var json = TransacaoMapper.map(transacaoResposta);
                ctx.result(json);
                ctx.status(200);
                ctx.contentType("application/json");
            } catch (Exception e) {
                ctx.status(422);
            }
        }).start(portNumber.get());
    }

    private Optional<Integer> getPortNumber() {
        var port = System.getenv("HTTP_PORT");
        if (Objects.isNull(port)) {
            //return Optional.empty();
            port = "8080";
        }
        return Optional.of(Integer.parseInt(port));
    }

}
