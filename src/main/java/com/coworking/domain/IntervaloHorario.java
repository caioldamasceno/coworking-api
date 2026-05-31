package com.coworking.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class IntervaloHorario {

    private final LocalTime inicio;
    private final LocalTime fim;

    public IntervaloHorario(LocalTime inicio, LocalTime fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    public LocalTime getInicio() {
        return inicio;
    }

    public LocalTime getFim() {
        return fim;
    }

    public boolean sobrepoe(IntervaloHorario outro) {
        return inicio.isBefore(outro.fim) && outro.inicio.isBefore(fim);
    }

    public List<IntervaloHorario> subtrair(List<IntervaloHorario> ocupados) {
        List<IntervaloHorario> livres = new ArrayList<>();
        LocalTime cursor = inicio;

        List<IntervaloHorario> ordenados = ocupados.stream()
                .sorted(Comparator.comparing(o -> o.inicio))
                .toList();

        for (IntervaloHorario ocupado : ordenados) {
            LocalTime ocupadoInicio = maximo(ocupado.inicio, inicio);
            LocalTime ocupadoFim = minimo(ocupado.fim, fim);
            if (!ocupadoFim.isAfter(ocupadoInicio)) {
                continue;
            }
            if (ocupadoInicio.isAfter(cursor)) {
                livres.add(new IntervaloHorario(cursor, ocupadoInicio));
            }
            cursor = maximo(cursor, ocupadoFim);
        }

        if (cursor.isBefore(fim)) {
            livres.add(new IntervaloHorario(cursor, fim));
        }
        return livres;
    }

    private static LocalTime maximo(LocalTime a, LocalTime b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalTime minimo(LocalTime a, LocalTime b) {
        return a.isBefore(b) ? a : b;
    }
}
