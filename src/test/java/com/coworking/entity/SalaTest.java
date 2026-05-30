package com.coworking.entity;

import com.coworking.enums.TipoSala;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SalaTest {

    @Test
    void construtorVazioMaisSetters_devemPreencherOsCampos() {
        Sala sala = new Sala();
        sala.setId(5L);
        sala.setNome("Sala Verde");
        sala.setTipo(TipoSala.INDIVIDUAL);
        sala.setCapacidade(4);

        assertThat(sala.getId()).isEqualTo(5L);
        assertThat(sala.getNome()).isEqualTo("Sala Verde");
        assertThat(sala.getTipo()).isEqualTo(TipoSala.INDIVIDUAL);
        assertThat(sala.getCapacidade()).isEqualTo(4);
    }

    @Test
    void construtorComArgumentos_devePreencherOsCampos() {
        Sala sala = new Sala("Auditorio", TipoSala.AUDITORIO, 120);

        assertThat(sala.getNome()).isEqualTo("Auditorio");
        assertThat(sala.getTipo()).isEqualTo(TipoSala.AUDITORIO);
        assertThat(sala.getCapacidade()).isEqualTo(120);
    }
}
