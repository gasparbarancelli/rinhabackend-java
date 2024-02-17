package com.gasparbarancelli.rinhabackend;

import java.time.format.DateTimeFormatter;

public class TransacaoMapper {

    private final static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");

    public TransacaoRequisicao map(String json) throws Exception {
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

    public String map(ExtratoResposta extratoResposta) {
        StringBuilder transacoes = new StringBuilder();
        for (ExtratoTransacaoResposta transacao : extratoResposta.transacoes()) {
            if (!transacoes.isEmpty()) {
                transacoes.append(", ");
            }
            transacoes.append("""
                    {
                      "valor": %d,
                      "tipo": "%s",
                      "descricao": "%s",
                      "realizada_em": "%s}"
                    }
                    """.formatted(
                            transacao.valor(),
                            transacao.tipo().name(),
                            transacao.descricao(),
                            DATETIME_FORMATTER.format(transacao.data())
                    )
            );
        }


        return """
            {
              "saldo": {
                "total": %d,
                "data_extrato": "%s",
                "limite": %d
              },
              "ultimas_transacoes": [
                %s
              ]
            }
            """.formatted(
                extratoResposta.saldo().total(),
                DATETIME_FORMATTER.format(extratoResposta.saldo().data()),
                extratoResposta.saldo().limite(),
                transacoes.toString()
            );
    }

    public String map(TransacaoResposta transacaoResposta) {
        return STR."""
                {
                    "limite": \{transacaoResposta.limite()},
                    "saldo": \{transacaoResposta.saldo()}
                }
                """;
    }

}
