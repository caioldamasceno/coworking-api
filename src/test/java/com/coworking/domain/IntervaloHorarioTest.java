package com.coworking.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class IntervaloHorarioTest {

    private IntervaloHorario intervalo(String inicio, String fim) {
        return new IntervaloHorario(LocalTime.parse(inicio), LocalTime.parse(fim));
    }

    @Test
    void sobreposicaoParcial_deveConflitar() {
        assertThat(intervalo("10:00", "11:00").sobrepoe(intervalo("10:30", "11:30"))).isTrue();
    }

    @Test
    void umIntervaloContidoNoOutro_deveConflitar() {
        assertThat(intervalo("10:00", "12:00").sobrepoe(intervalo("10:30", "11:00"))).isTrue();
        assertThat(intervalo("10:30", "11:00").sobrepoe(intervalo("10:00", "12:00"))).isTrue();
    }

    @Test
    void horariosIdenticos_deveConflitar() {
        assertThat(intervalo("10:00", "11:00").sobrepoe(intervalo("10:00", "11:00"))).isTrue();
    }

    @Test
    void encostandoNoFim_naoDeveConflitar() {
        assertThat(intervalo("10:00", "11:00").sobrepoe(intervalo("11:00", "12:00"))).isFalse();
    }

    @Test
    void encostandoNoInicio_naoDeveConflitar() {
        assertThat(intervalo("11:00", "12:00").sobrepoe(intervalo("10:00", "11:00"))).isFalse();
    }

    @Test
    void totalmenteSeparados_naoDeveConflitar() {
        assertThat(intervalo("10:00", "11:00").sobrepoe(intervalo("14:00", "15:00"))).isFalse();
    }

    @Test
    void aRegraDeveSerComutativa() {
        IntervaloHorario a = intervalo("10:00", "11:00");
        IntervaloHorario b = intervalo("10:30", "11:30");
        assertThat(a.sobrepoe(b)).isEqualTo(b.sobrepoe(a));
    }
}
