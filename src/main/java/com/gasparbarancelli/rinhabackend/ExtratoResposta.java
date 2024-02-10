package com.gasparbarancelli.rinhabackend;


import java.time.LocalDateTime;
import java.util.List;

public record ExtratoResposta(
        ExtratoSaldoResposta saldo,
        List<ExtratoTransacaoResposta> transacoes) {

    public record ExtratoSaldoResposta(
            int total,
            LocalDateTime data,
            int limite) {

    }

    public record ExtratoTransacaoResposta(
            int valor,
            TipoTransacao tipo,
            String descricao,
            LocalDateTime data) {

        public static ExtratoTransacaoResposta gerar(Transacao transacao) {
            return new ExtratoResposta.ExtratoTransacaoResposta(
                    transacao.valor(),
                    transacao.tipo(),
                    transacao.descricao(),
                    transacao.data());
        }

    }

}