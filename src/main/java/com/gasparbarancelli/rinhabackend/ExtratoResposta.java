package com.gasparbarancelli.rinhabackend;


import java.util.List;

public record ExtratoResposta(
        ExtratoSaldoResposta saldo,
        List<ExtratoTransacaoResposta> transacoes) {

}
