package com.gasparbarancelli.rinhabackend;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

final class DataSource {

    private static final HikariDataSource hikariDataSource;

    private static final String SQL_CLIENTE_FIND_BY_ID = """
                SELECT limite, valor
                FROM CLIENTE
                WHERE id = ?
            """;

    private static final String SQL_TRANSACAO_FIND = """
                select t
                from Transacao t
                where t.cliente = :cliente
                order by t.data desc
                limit 10
            """;

    private static final String SQL_CLIENTE_FIND_BY_ID_FOR_UPDATE = """
                SELECT limite, valor
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

    private static final String SQL_UPDATE_CLIENTE = "UPDATE CLIENTE SET VALOR = VALOR + ? WHERE ID = ?";

    static {
        var host = Optional.ofNullable(System.getenv("DATABASE_HOST"))
                .orElse("db");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + host + "/RINHA_BACKEND?loggerLevel=OFF");
        config.setUsername("postgres");
        config.setPassword("postgres");
        config.addDataSourceProperty("maximumPoolSize", "200");
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

    static ExtratoResposta extrato(int id) {
        try (var con = hikariDataSource.getConnection();
             var stmt = con.prepareStatement(SQL_CLIENTE_FIND_BY_ID)) {
            stmt.setObject(1, id);
            try (var resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    var limite = resultSet.getInt(1);
                    var valor = resultSet.getInt(2);

                    var cliente = new Cliente(
                            id,
                            limite,
                            valor
                    );
                    return Optional.of(cliente);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static void insert(Transacao transacao) throws SQLException {
        try (var con = hikariDataSource.getConnection();
             var stsSelect = con.prepareStatement(SQL_CLIENTE_FIND_BY_ID_FOR_UPDATE);
             var stmt = con.prepareStatement(SQL_INSERT_TRANSACAO);
             var stmtUpdateCliente = con.prepareStatement(SQL_UPDATE_CLIENTE)) {
            con.setAutoCommit(false);

            stsSelect.setObject(1, transacao.cliente());
            stsSelect.executeQuery();

            stmt.setInt(1, transacao.cliente());
            stmt.setInt(2, transacao.valor());
            stmt.setString(3, transacao.tipo().name());
            stmt.setString(4, transacao.descricao());
            stmt.setTimestamp(5, Timestamp.valueOf(transacao.data()));
            stmt.executeUpdate();

            stmtUpdateCliente.setInt(1, transacao.valor());
            stmtUpdateCliente.setInt(2, transacao.cliente());
            stmtUpdateCliente.executeUpdate();

            con.commit();
        }
    }

}
