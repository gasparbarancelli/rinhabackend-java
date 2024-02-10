package com.gasparbarancelli.rinhabackend;

import java.time.LocalDateTime;

public record TransacaoRequisicao(
        Integer valor,
        TipoTransacao tipo,
        String descricao
) {

    public boolean ehValido() {
        return Valida.valor.test(valor)
                && tipo != null
                && Valida.descricao.test(descricao);
    }

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