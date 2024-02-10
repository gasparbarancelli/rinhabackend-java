package com.gasparbarancelli.rinhabackend;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record ExtratoTransacaoResposta(
            int valor,
            TipoTransacao tipo,
            String descricao,
            @JsonProperty("realizada_em")
            @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'")
            LocalDateTime data) {

}