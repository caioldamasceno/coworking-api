package com.coworking.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

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

    @Test
    void subtrair_semOcupados_deveRetornarIntervaloInteiro() {
        List<IntervaloHorario> livres = intervalo("08:00", "22:00").subtrair(List.of());

        assertThat(livres).extracting(IntervaloHorario::getInicio, IntervaloHorario::getFim)
                .containsExactly(tuple(LocalTime.parse("08:00"), LocalTime.parse("22:00")));
    }

    @Test
    void subtrair_comUmaReservaNoMeio_deveRetornarOsDoisLados() {
        List<IntervaloHorario> livres = intervalo("08:00", "22:00")
                .subtrair(List.of(intervalo("10:00", "11:00")));

        assertThat(livres).extracting(IntervaloHorario::getInicio, IntervaloHorario::getFim)
                .containsExactly(
                        tuple(LocalTime.parse("08:00"), LocalTime.parse("10:00")),
                        tuple(LocalTime.parse("11:00"), LocalTime.parse("22:00")));
    }

    @Test
    void subtrair_comReservaNoInicio_deveRetornarApenasOResto() {
        List<IntervaloHorario> livres = intervalo("08:00", "22:00")
                .subtrair(List.of(intervalo("08:00", "09:00")));

        assertThat(livres).extracting(IntervaloHorario::getInicio, IntervaloHorario::getFim)
                .containsExactly(tuple(LocalTime.parse("09:00"), LocalTime.parse("22:00")));
    }

    @Test
    void subtrair_comReservasSobrepostasEForaDeOrdem_deveMesclarOsOcupados() {
        List<IntervaloHorario> livres = intervalo("08:00", "22:00")
                .subtrair(List.of(intervalo("13:00", "14:00"), intervalo("10:00", "11:00"), intervalo("10:30", "12:00")));

        assertThat(livres).extracting(IntervaloHorario::getInicio, IntervaloHorario::getFim)
                .containsExactly(
                        tuple(LocalTime.parse("08:00"), LocalTime.parse("10:00")),
                        tuple(LocalTime.parse("12:00"), LocalTime.parse("13:00")),
                        tuple(LocalTime.parse("14:00"), LocalTime.parse("22:00")));
    }

    @Test
    void subtrair_quandoOcupadoCobreTudo_deveRetornarVazio() {
        List<IntervaloHorario> livres = intervalo("08:00", "22:00")
                .subtrair(List.of(intervalo("08:00", "22:00")));

        assertThat(livres).isEmpty();
    }

    @Test
    void subtrair_deveIgnorarReservasForaDaJanela() {
        List<IntervaloHorario> livres = intervalo("08:00", "22:00")
                .subtrair(List.of(intervalo("06:00", "07:00")));

        assertThat(livres).extracting(IntervaloHorario::getInicio, IntervaloHorario::getFim)
                .containsExactly(tuple(LocalTime.parse("08:00"), LocalTime.parse("22:00")));
    }
}
