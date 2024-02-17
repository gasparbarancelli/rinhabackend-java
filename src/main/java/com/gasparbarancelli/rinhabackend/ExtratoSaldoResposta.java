package com.gasparbarancelli.rinhabackend;

import java.time.LocalDateTime;

public record ExtratoSaldoResposta(
            int total,
            LocalDateTime data,
            int limite) {

}