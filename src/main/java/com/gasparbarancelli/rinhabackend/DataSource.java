package com.gasparbarancelli.rinhabackend;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class DataSource {

    private static final HikariDataSource hikariDataSource;

    private static final String SQL_CLIENTE_FIND_BY_ID = """
                SELECT LIMITE, SALDO
                FROM CLIENTE
                WHERE id = ?
            """;

    private static final String SQL_TRANSACAO_FIND = """
                SELECT VALOR, TIPO, DESCRICAO, DATA
                FROM TRANSACAO
                WHERE CLIENTE_ID = ?
                ORDER BY DATA DESC
                LIMIT 10;
            """;

    private static final String SQL_CLIENTE_FIND_BY_ID_FOR_UPDATE = """
                SELECT LIMITE, SALDO
                FROM CLIENTE
                WHERE id = ?
                FOR UPDATE;
            """;

    private static final String SQL_INSERT_TRANSACAO = """
            INSERT INTO TRANSACAO (
                CLIENTE_ID,
                VALOR,
                TIPO,
                DESCRICAO,
                DATA
            )
            VALUES (?, ?, ?, ?, ?)
        """;

    private static final String SQL_UPDATE_CLIENTE = "UPDATE CLIENTE SET SALDO = ? WHERE ID = ?";

    static {
        var host = Optional.ofNullable(System.getenv("DATABASE_HOST"))
                .orElse("localhost");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + host + "/rinha-backend?loggerLevel=OFF");
        config.setUsername("rinha");
        config.setPassword("backend");
        config.addDataSourceProperty("minimumIdle", "5");
        config.addDataSourceProperty("maximumPoolSize", "5");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        hikariDataSource = new HikariDataSource(config);
    }

    private DataSource() {
    }

    static ExtratoResposta extrato(int clienteId) {
        try (var con = hikariDataSource.getConnection();
             var stmtFindCliente = con.prepareStatement(SQL_CLIENTE_FIND_BY_ID);
             var stmtFindTransacoes = con.prepareStatement(SQL_TRANSACAO_FIND)) {
            var cliente = getCliente(clienteId, stmtFindCliente);
            var transacoes = getTransacoes(clienteId, stmtFindTransacoes);

            var saldo = new ExtratoSaldoResposta(
                    cliente.saldo(),
                    LocalDateTime.now(),
                    cliente.limite()
            );

            return new ExtratoResposta(
                    saldo,
                    transacoes
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Cliente getCliente(int clienteId, PreparedStatement statement) throws SQLException {
        statement.setObject(1, clienteId);
        try (var resultSet = statement.executeQuery()) {
            resultSet.next();

            var limite = resultSet.getInt(1);
            var saldo = resultSet.getInt(2);

            return new Cliente(
                    clienteId,
                    limite,
                    saldo
            );
        }
    }

    private static List<ExtratoTransacaoResposta> getTransacoes(int clienteId, PreparedStatement statement) throws SQLException {
        statement.setObject(1, clienteId);
        try (var resultSet = statement.executeQuery()) {
            List<ExtratoTransacaoResposta> transacoes = new ArrayList<>(resultSet.getFetchSize());
            while (resultSet.next()) {
                var valor = resultSet.getInt(1);
                var tipo = resultSet.getString(2);
                var descricao = resultSet.getString(3);
                var data = resultSet.getTimestamp(4);


                var transacao = new ExtratoTransacaoResposta(
                        valor,
                        TipoTransacao.valueOf(tipo),
                        descricao,
                        data.toLocalDateTime()
                );
                transacoes.add(transacao);
            }
            return transacoes;
        }
    }

    static TransacaoResposta insert(Transacao transacao) throws Exception {
        try (var con = hikariDataSource.getConnection();
             var stmtClienteFind = con.prepareStatement(SQL_CLIENTE_FIND_BY_ID_FOR_UPDATE)) {
            con.setAutoCommit(false);

            var cliente = getCliente(1, stmtClienteFind);
            var saldo = getSaldoAtualizado(transacao, cliente);

            try (var stmtTransacaoInsert = con.prepareStatement(SQL_INSERT_TRANSACAO);
                 var stmtClienteUpdate = con.prepareStatement(SQL_UPDATE_CLIENTE)) {
                stmtTransacaoInsert.setInt(1, transacao.cliente());
                stmtTransacaoInsert.setInt(2, transacao.valor());
                stmtTransacaoInsert.setString(3, transacao.tipo().name());
                stmtTransacaoInsert.setString(4, transacao.descricao());
                stmtTransacaoInsert.setTimestamp(5, Timestamp.valueOf(transacao.data()));
                stmtTransacaoInsert.executeUpdate();

                stmtClienteUpdate.setInt(1, transacao.valor());
                stmtClienteUpdate.setInt(2, transacao.cliente());
                stmtClienteUpdate.executeUpdate();

                con.commit();
            }

            return new TransacaoResposta(cliente.limite(), saldo);
        }
    }

    private static int getSaldoAtualizado(Transacao transacao, Cliente cliente) throws Exception {
        if (TipoTransacao.d.equals(transacao.tipo())) {
            if (cliente.getSaldoComLimite() < transacao.valor()) {
                throw new Exception("Sem limite disponivies");
            }
            return cliente.saldo() - transacao.valor();
        }
        return cliente.saldo() + transacao.valor();
    }

}
