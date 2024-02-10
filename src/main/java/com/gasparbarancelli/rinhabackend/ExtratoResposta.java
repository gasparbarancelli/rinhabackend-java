package com.gasparbarancelli.rinhabackend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ExtratoResposta(
        ExtratoSaldoResposta saldo,
        @JsonProperty("ultimas_transacoes")
        List<ExtratoTransacaoResposta> transacoes) {

}
