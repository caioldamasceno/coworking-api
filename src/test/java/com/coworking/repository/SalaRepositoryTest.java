package com.coworking.repository;

import com.coworking.entity.Sala;
import com.coworking.enums.TipoSala;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class SalaRepositoryTest {

    @Autowired
    private SalaRepository salaRepository;

    @Test
    void findByTipo_deveRetornarApenasAsSalasDoTipoInformado() {
        salaRepository.save(new Sala("Foco 1", TipoSala.INDIVIDUAL, 4));
        salaRepository.save(new Sala("Foco 2", TipoSala.INDIVIDUAL, 3));
        salaRepository.save(new Sala("Coletiva", TipoSala.COLETIVA, 10));

        Page<Sala> individuais = salaRepository.findByTipo(TipoSala.INDIVIDUAL, PageRequest.of(0, 20));

        assertThat(individuais.getContent()).extracting(Sala::getNome).containsExactlyInAnyOrder("Foco 1", "Foco 2");
    }

    @Test
    void findByTipo_deveRespeitarOTamanhoDaPagina() {
        salaRepository.save(new Sala("Foco 1", TipoSala.INDIVIDUAL, 4));
        salaRepository.save(new Sala("Foco 2", TipoSala.INDIVIDUAL, 3));
        salaRepository.save(new Sala("Foco 3", TipoSala.INDIVIDUAL, 5));

        Page<Sala> primeiraPagina = salaRepository.findByTipo(TipoSala.INDIVIDUAL, PageRequest.of(0, 2));

        assertThat(primeiraPagina.getContent()).hasSize(2);
        assertThat(primeiraPagina.getTotalElements()).isEqualTo(3);
        assertThat(primeiraPagina.getTotalPages()).isEqualTo(2);
    }

    @Test
    void findByTipo_quandoNaoHaSalasDoTipo_deveRetornarVazio() {
        salaRepository.save(new Sala("Coletiva", TipoSala.COLETIVA, 10));

        assertThat(salaRepository.findByTipo(TipoSala.AUDITORIO, PageRequest.of(0, 20)).getContent()).isEmpty();
    }

    @Test
    void salvar_comNomeJaExistente_deveViolarRestricaoDeUnicidade() {
        salaRepository.saveAndFlush(new Sala("Sala Azul", TipoSala.COLETIVA, 10));

        assertThatThrownBy(() -> salaRepository.saveAndFlush(new Sala("Sala Azul", TipoSala.INDIVIDUAL, 4)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
