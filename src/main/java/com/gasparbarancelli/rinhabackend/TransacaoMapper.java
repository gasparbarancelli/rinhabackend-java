package com.gasparbarancelli.rinhabackend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;

final class TransacaoMapper {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(String.class, new StringValueAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private TransacaoMapper() {
    }

    static TransacaoRequisicao map(String json) throws Exception {
        var transacaoRequisicao = GSON.fromJson(json, TransacaoRequisicao.class);
        if (!transacaoRequisicao.ehValido()) {
            throw new Exception("Dados invalidos");
        }
        return transacaoRequisicao;
    }

    static String map(ExtratoResposta extratoResposta) {
        return GSON.toJson(extratoResposta);
    }

    static String map(TransacaoResposta transacaoResposta) {
        return GSON.toJson(transacaoResposta);
    }

}
