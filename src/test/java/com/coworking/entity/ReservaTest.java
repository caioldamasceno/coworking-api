package com.coworking.entity;

import com.coworking.enums.TipoSala;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservaTest {

    @Test
    void construtorVazioMaisSetters_devemPreencherOsCampos() {
        Sala sala = new Sala("Sala Azul", TipoSala.COLETIVA, 10);
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setSala(sala);
        reserva.setData(LocalDate.of(2026, 6, 1));
        reserva.setHoraInicio(LocalTime.of(9, 0));
        reserva.setHoraFim(LocalTime.of(10, 0));
        reserva.setResponsavel("Caio");
        reserva.setEmail("caio@email.com");

        assertThat(reserva.getId()).isEqualTo(1L);
        assertThat(reserva.getSala()).isEqualTo(sala);
        assertThat(reserva.getData()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(reserva.getHoraInicio()).isEqualTo(LocalTime.of(9, 0));
        assertThat(reserva.getHoraFim()).isEqualTo(LocalTime.of(10, 0));
        assertThat(reserva.getResponsavel()).isEqualTo("Caio");
        assertThat(reserva.getEmail()).isEqualTo("caio@email.com");
    }

    @Test
    void construtorComArgumentos_devePreencherOsCampos() {
        Sala sala = new Sala("Auditorio", TipoSala.AUDITORIO, 40);
        Reserva reserva = new Reserva(sala, LocalDate.of(2026, 6, 1),
                LocalTime.of(14, 0), LocalTime.of(15, 30), "Ana", "ana@email.com");

        assertThat(reserva.getSala()).isEqualTo(sala);
        assertThat(reserva.getData()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(reserva.getHoraInicio()).isEqualTo(LocalTime.of(14, 0));
        assertThat(reserva.getHoraFim()).isEqualTo(LocalTime.of(15, 30));
        assertThat(reserva.getResponsavel()).isEqualTo("Ana");
        assertThat(reserva.getEmail()).isEqualTo("ana@email.com");
    }
}
