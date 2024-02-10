package com.gasparbarancelli.rinhabackend;

public record Cliente(
        int id,
        int limite,
        int saldo
) {

    public static boolean naoExiste(int id) {
        return id < 1 || id > 5;
    }

}
