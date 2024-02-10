package com.gasparbarancelli.rinhabackend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

final class TransacaoMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    private TransacaoMapper() {
    }

    static TransacaoRequisicao map(String json) throws Exception {
        var transacaoRequisicao = OBJECT_MAPPER.readValue(json, TransacaoRequisicao.class);
        if (!transacaoRequisicao.ehValido()) {
            throw new Exception("Dados invalidos");
        }
        return transacaoRequisicao;
    }

    static String map(ExtratoResposta extratoResposta) {
        try {
            return OBJECT_MAPPER.writeValueAsString(extratoResposta);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static String map(TransacaoResposta transacaoResposta) {
        try {
            return OBJECT_MAPPER.writeValueAsString(transacaoResposta);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
