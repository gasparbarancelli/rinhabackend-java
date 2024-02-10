package com.gasparbarancelli.rinhabackend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.logging.Level;
import java.util.logging.Logger;

final class TransacaoMapper {

    private static final Logger LOGGER = Logger.getLogger(TransacaoMapper.class.getName());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    private TransacaoMapper() {
    }

    static TransacaoRequisicao map(String json) throws Exception {
        var transacaoRequisicao = OBJECT_MAPPER.readValue(json, TransacaoRequisicao.class);
        LOGGER.log(Level.INFO, transacaoRequisicao.toString());
        if (!transacaoRequisicao.ehValido()) {
            throw new Exception("Dados invalidos");
        }
        return transacaoRequisicao;
    }

    static String map(ExtratoResposta extratoResposta) {
        try {
            return OBJECT_MAPPER.writeValueAsString(extratoResposta);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    static String map(TransacaoResposta transacaoResposta) {
        try {
            return OBJECT_MAPPER.writeValueAsString(transacaoResposta);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
