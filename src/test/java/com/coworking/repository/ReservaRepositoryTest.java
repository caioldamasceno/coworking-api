package com.coworking.repository;

import com.coworking.entity.Reserva;
import com.coworking.entity.Sala;
import com.coworking.enums.TipoSala;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservaRepositoryTest {

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    private static final LocalDate HOJE = LocalDate.of(2026, 6, 1);

    @Test
    void existsBySalaIdAndDataGreaterThanEqual_comReservaFutura_deveSerTrue() {
        Sala sala = salaRepository.save(new Sala("Sala", TipoSala.COLETIVA, 10));
        reservaRepository.save(new Reserva(sala, HOJE.plusDays(2), LocalTime.of(9, 0), LocalTime.of(10, 0), "Ana", "ana@email.com"));

        assertThat(reservaRepository.existsBySalaIdAndDataGreaterThanEqual(sala.getId(), HOJE)).isTrue();
    }

    @Test
    void existsBySalaIdAndDataGreaterThanEqual_naDataDeReferencia_deveSerTrue() {
        Sala sala = salaRepository.save(new Sala("Sala", TipoSala.COLETIVA, 10));
        reservaRepository.save(new Reserva(sala, HOJE, LocalTime.of(9, 0), LocalTime.of(10, 0), "Ana", "ana@email.com"));

        assertThat(reservaRepository.existsBySalaIdAndDataGreaterThanEqual(sala.getId(), HOJE)).isTrue();
    }

    @Test
    void existsBySalaIdAndDataGreaterThanEqual_apenasComReservaPassada_deveSerFalse() {
        Sala sala = salaRepository.save(new Sala("Sala", TipoSala.COLETIVA, 10));
        reservaRepository.save(new Reserva(sala, HOJE.minusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0), "Ana", "ana@email.com"));

        assertThat(reservaRepository.existsBySalaIdAndDataGreaterThanEqual(sala.getId(), HOJE)).isFalse();
    }
}
