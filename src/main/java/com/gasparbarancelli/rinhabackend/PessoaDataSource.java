package com.gasparbarancelli.rinhabackend;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

final class PessoaDataSource {

    private static final HikariDataSource hikariDataSource;

    private static final String SQL_COUNT = "SELECT count(*) FROM pessoa";

    private static final String SQL_FIND_BY_ID = """
                SELECT apelido, nome, nascimento, stack
                FROM pessoa
                WHERE publicid = ?
            """;

    private static final String SQL_FIND_BY_TERM = """
                SELECT publicid, apelido, nome, nascimento, stack
                FROM pessoa
                WHERE BUSCA_TRGM LIKE ?
                LIMIT 50
            """;

    private static final String SQL_INSERT_PESSOA = """
            INSERT INTO PESSOA (publicid, apelido, nome, nascimento, stack)
            VALUES (?, ?, ?, ?, ?)
        """;

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

    private PessoaDataSource() {
    }

    static long count() {
        try (var con = hikariDataSource.getConnection();
             var stmt = con.prepareStatement(SQL_COUNT);
             var resultSet = stmt.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static List<Pessoa> findByTerm(String search) {
        try (var con = hikariDataSource.getConnection();
             var stmt = con.prepareStatement(SQL_FIND_BY_TERM, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            stmt.setString(1, "%" + search + "%");
            try (var resultSet = stmt.executeQuery()) {
                resultSet.last();
                var pessoas = new ArrayList<Pessoa>(resultSet.getRow());
                resultSet.beforeFirst();

                while (resultSet.next()) {
                    var id = resultSet.getObject(1);
                    var apelido = resultSet.getString(2);
                    var nome = resultSet.getString(3);
                    var nascimento = resultSet.getDate(4);
                    var stacksJoin = resultSet.getString(5);
                    var pessoa = new Pessoa(
                            UUID.fromString(id.toString()),
                            apelido,
                            nome,
                            nascimento.toLocalDate(),
                            stackJoinToList(stacksJoin)
                    );
                    pessoas.add(pessoa);
                }
                return pessoas;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> stackJoinToList(String stacksJoin) {
        return stacksJoin != null
                ? Arrays.asList(stacksJoin.split(","))
                : null;
    }

    static Optional<Pessoa> findById(UUID id) {
        try (var con = hikariDataSource.getConnection();
             var stmt = con.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setObject(1, id);
            try (var resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    var apelido = resultSet.getString(1);
                    var nome = resultSet.getString(2);
                    var nascimento = resultSet.getDate(3);
                    var stacksJoin = resultSet.getString(4);

                    var pessoa = new Pessoa(
                            id,
                            apelido,
                            nome,
                            nascimento.toLocalDate(),
                            stackJoinToList(stacksJoin)
                    );
                    return Optional.of(pessoa);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static String insert(Pessoa pessoa) throws SQLException {
        try (var con = hikariDataSource.getConnection();
             var stmt = con.prepareStatement(SQL_INSERT_PESSOA)) {
            var pessoaId = UUID.randomUUID();

            stmt.setObject(1, pessoaId);
            stmt.setString(2, pessoa.apelido());
            stmt.setString(3, pessoa.nome());
            stmt.setDate(4, Date.valueOf(pessoa.nascimento()));
            stmt.setString(5, pessoa.stacksJoin());
            stmt.executeUpdate();

            return pessoaId.toString();
        }
    }

    private record PessoaModel(
            String id,
            String apelido,
            String nome,
            LocalDate nascimento,
            String stack
    ) {
    }

}
