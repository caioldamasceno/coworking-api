package com.coworking.domain;

import java.time.LocalTime;

public class IntervaloHorario {

    private final LocalTime inicio;
    private final LocalTime fim;

    public IntervaloHorario(LocalTime inicio, LocalTime fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    public boolean sobrepoe(IntervaloHorario outro) {
        return inicio.isBefore(outro.fim) && outro.inicio.isBefore(fim);
    }
}
