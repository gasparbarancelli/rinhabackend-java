package com.gasparbarancelli.rinhabackend;

import java.util.Objects;
import java.util.function.Predicate;

public class Valida {

    private Valida() {
    }

    public static Predicate<Integer> valor = (valor) -> {
        if (Objects.isNull(valor)) {
            return false;
        }

        return valor > 0;
    };

    public static Predicate<String> descricao = (descricao) ->
            Objects.nonNull(descricao)
            && !descricao.isEmpty()
            && descricao.length() <= 10;

}