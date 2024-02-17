package com.gasparbarancelli.rinhabackend;

import java.time.format.DateTimeFormatter;

import static java.lang.StringTemplate.STR;

final class TransacaoMapper {

    final static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");

    private TransacaoMapper() {
    }

    static TransacaoRequisicao map(String json) throws Exception {
        json = json.replaceAll("[{}\"]", "");
        String[] keyValuePairs = json.split(",");

        int valor = 0;
        TipoTransacao tipo = null;
        String descricao = null;

        for (String pair : keyValuePairs) {
            String[] entry = pair.split(":");
            String key = entry[0].trim();
            String value = entry[1].trim();

            if (key.equals("valor")) {
                if (!Valida.valor.test(value)) {
                    throw new Exception("Dados invalidos");
                }
                valor = Integer.parseInt(value);
            } else if (key.equals("tipo")) {
                tipo = TipoTransacao.valueOf(value);
            } else if (key.equals("descricao")) {
                if (!Valida.descricao.test(value)) {
                    throw new Exception("Dados invalidos");
                }
                descricao = value;
            }
        }

        return new TransacaoRequisicao(valor, tipo, descricao);
    }

    static String map(ExtratoResposta extratoResposta) {
        StringBuilder transacoes = new StringBuilder();
        extratoResposta.transacoes().forEach(transacao -> {
            if (!transacoes.isEmpty()) {
                transacoes.append(", ");
            }
            transacoes.append(STR."""
                    {
                      "valor": \{transacao.valor()},
                      "tipo": "\{transacao.tipo().name()}",
                      "descricao": "\{transacao.descricao()}",
                      "realizada_em": "\{DATETIME_FORMATTER.format(transacao.data())}"
                    }
                    """);
        });


        return STR."""
            {
              "saldo": {
                "total": \{extratoResposta.saldo().total()},
                "data_extrato": "\{DATETIME_FORMATTER.format(extratoResposta.saldo().data())}",
                "limite": \{extratoResposta.saldo().limite()}
              },
              "ultimas_transacoes": [
                \{transacoes.toString()}
              ]
            }
            """;
    }

    static String map(TransacaoResposta transacaoResposta) {
        return STR."""
                {
                    "limite": \{transacaoResposta.limite()},
                    "saldo": \{transacaoResposta.saldo()}
                }
                """;
    }

}
