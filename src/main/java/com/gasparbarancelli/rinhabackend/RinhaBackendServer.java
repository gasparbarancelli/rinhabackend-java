package com.gasparbarancelli.rinhabackend;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import static spark.Spark.*;

public class RinhaBackendServer {

    private static final Logger LOGGER = Logger.getLogger(RinhaBackendServer.class.getName());

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

        LOGGER.info("Servidor http iniciado na porta " + portNumber.get());

        DataSource dataSource = new DataSource();

        port(portNumber.get());

        get("/clientes/:clienteId/extrato", (request, response) -> {
            var clienteId = Integer.parseInt(request.params(":clienteId"));
            if (Cliente.naoExiste(clienteId)) {
                response.status(404);
                return "cliente nao existe";
            }
            var extrato = dataSource.extrato(clienteId);
            var json = TransacaoMapper.map(extrato);
            response.status(200);
            response.type("application/json");
            return json;
        });

        post("/clientes/{clienteId}/transacoes", (request, response) -> {
            try {
                var clienteId = Integer.parseInt(request.params(":clienteId"));
                if (Cliente.naoExiste(clienteId)) {
                    response.status(404);
                    return "cliente nao existe";
                }
                var body = request.body();
                var transacaoRequisicao = TransacaoMapper.map(body);
                var transacaoResposta = dataSource.insert(transacaoRequisicao.geraTransacao(clienteId));

                var json = TransacaoMapper.map(transacaoResposta);
                response.status(200);
                response.type("application/json");
                return json;
            } catch (Exception e) {
                response.status(422);
                return "dados invalidos";
            }
        });
    }

    private Optional<Integer> getPortNumber() {
        var port = System.getenv("HTTP_PORT");
        if (Objects.isNull(port)) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(port));
    }

}
