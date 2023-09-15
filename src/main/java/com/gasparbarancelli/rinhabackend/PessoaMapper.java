package com.gasparbarancelli.rinhabackend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.util.List;

final class PessoaMapper {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(String.class, new StringValueAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    private PessoaMapper() {
    }

    static Pessoa map(String json) throws Exception {
        var pessoa = GSON.fromJson(json, Pessoa.class);
        if (!pessoa.isValid()) {
            throw new Exception("Dados invalidos");
        }
        return pessoa;
    }

    static String map(List<Pessoa> pessoas) {
        return GSON.toJson(pessoas);
    }

    static String map(Pessoa pessoa) {
        return GSON.toJson(pessoa);
    }

}
