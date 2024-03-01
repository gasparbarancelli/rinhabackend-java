package com.gasparbarancelli.rinhabackend;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataSource {

    private final HikariDataSource hikariDataSource;

    private final String SQL_CLIENTE_FIND_BY_ID = """
                SELECT LIMITE, SALDO
                FROM CLIENTE
                WHERE id = ?
            """;

    private final String SQL_TRANSACAO_FIND = """
                SELECT VALOR, TIPO, DESCRICAO, DATA
                FROM TRANSACAO
                WHERE CLIENTE_ID = ?
                ORDER BY DATA DESC
                LIMIT 10;
            """;

    private final String SQL_INSERT_TRANSACAO = "SELECT saldoRetorno, limiteRetorno FROM efetuar_transacao(?, ?, ?, ?)";

    public DataSource() {
        var host = Optional.ofNullable(System.getenv("DATABASE_HOST"))
                .orElse("localhost");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + host + "/rinha-backend?loggerLevel=OFF");
        config.setUsername("rinha");
        config.setPassword("backend");
        config.setConnectionInitSql("SELECT 1");
        config.setMinimumIdle(15);
        config.setMaximumPoolSize(15);
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
        warmup();
    }

    public void warmup() {
        for (int i = 0; i < 15; i++) {
            try (var connection = hikariDataSource.getConnection()) {
                // Connection is borrowed and immediately returned
            } catch (SQLException ignore) {

            }
        }
    }

    public ExtratoResposta extrato(int clienteId) {
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

    private Cliente getCliente(int clienteId, PreparedStatement statement) throws SQLException {
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

    private List<ExtratoTransacaoResposta> getTransacoes(int clienteId, PreparedStatement statement) throws SQLException {
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

    public TransacaoResposta insert(Transacao transacao) throws Exception {
        try (var con = hikariDataSource.getConnection();
             CallableStatement statement = con.prepareCall(SQL_INSERT_TRANSACAO)) {
            statement.setInt(1, transacao.cliente());
            statement.setString(2, transacao.tipo().name());
            statement.setInt(3, transacao.valor());
            statement.setString(4, transacao.descricao());
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                int novoSaldo = resultSet.getInt(1);
                int limite = resultSet.getInt(2);
                return new TransacaoResposta(
                        limite,
                        novoSaldo
                );
            }
        }
    }

}
