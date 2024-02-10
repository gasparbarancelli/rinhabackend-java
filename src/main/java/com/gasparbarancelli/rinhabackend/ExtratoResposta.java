package com.gasparbarancelli.rinhabackend;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record ExtratoResposta(
        ExtratoSaldoResposta saldo,
        @JsonProperty("ultimas_transacoes")
        List<ExtratoTransacaoResposta> transacoes) {

    public record ExtratoSaldoResposta(
            int total,
            @JsonProperty("data_extrato")
            @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'")
            LocalDateTime data,
            int limite) {

    }

    public record ExtratoTransacaoResposta(
            int valor,
            TipoTransacao tipo,
            String descricao,
            @JsonProperty("realizada_em")
            @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'")
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
