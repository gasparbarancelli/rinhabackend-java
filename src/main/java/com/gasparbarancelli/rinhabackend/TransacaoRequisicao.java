package com.gasparbarancelli.rinhabackend;

import java.time.LocalDateTime;

public record TransacaoRequisicao(
        int valor,
        TipoTransacao tipo,
        String descricao
) {

    public Transacao geraTransacao(int cliente) {
        return new Transacao(
                cliente,
                valor,
                tipo,
                descricao,
                LocalDateTime.now()
        );
    }

}
