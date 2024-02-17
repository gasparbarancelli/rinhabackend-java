package com.gasparbarancelli.rinhabackend;

import java.time.LocalDateTime;

public record ExtratoTransacaoResposta(
            int valor,
            TipoTransacao tipo,
            String descricao,
            LocalDateTime data) {

}