package com.coworking.domain;

import java.time.LocalTime;

public final class Funcionamento {

    public static final LocalTime ABERTURA = LocalTime.of(8, 0);
    public static final LocalTime FECHAMENTO = LocalTime.of(22, 0);

    private Funcionamento() {
    }

    public static IntervaloHorario intervalo() {
        return new IntervaloHorario(ABERTURA, FECHAMENTO);
    }

    public static boolean contem(LocalTime inicio, LocalTime fim) {
        return !inicio.isBefore(ABERTURA) && !fim.isAfter(FECHAMENTO);
    }
}
