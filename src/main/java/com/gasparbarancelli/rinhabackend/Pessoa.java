package com.gasparbarancelli.rinhabackend;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

record Pessoa(
        UUID id,
        String apelido,
        String nome,
        LocalDate nascimento,
        List<String> stack
) {

    boolean isValid() {
        return apelido.length() <= 32
                && nome.length() <= 100
                && (stack == null || stack.stream().allMatch(it -> it.length() <= 32));
    }

    String stacksJoin() {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return String.join(",", stack);
    }

}
