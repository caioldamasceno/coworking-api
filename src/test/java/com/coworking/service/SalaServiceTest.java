package com.coworking.service;

import com.coworking.dto.request.SalaRequestDTO;
import com.coworking.dto.response.SalaResponseDTO;
import com.coworking.entity.Sala;
import com.coworking.enums.TipoSala;
import com.coworking.mapper.SalaMapper;
import com.coworking.repository.SalaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalaServiceTest {

    @Mock
    private SalaRepository salaRepository;

    private SalaService salaService;

    @BeforeEach
    void setUp() {
        salaService = new SalaServiceImpl(salaRepository, new SalaMapper());
    }

    @Test
    void cadastrar_devePersistirERetornarDtoComIdGerado() {
        SalaRequestDTO request = new SalaRequestDTO("Sala Azul", TipoSala.COLETIVA, 10);
        Sala salva = new Sala("Sala Azul", TipoSala.COLETIVA, 10);
        salva.setId(1L);
        when(salaRepository.save(any(Sala.class))).thenReturn(salva);

        SalaResponseDTO resposta = salaService.cadastrar(request);

        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.nome()).isEqualTo("Sala Azul");
        assertThat(resposta.tipo()).isEqualTo(TipoSala.COLETIVA);
        assertThat(resposta.capacidade()).isEqualTo(10);
        verify(salaRepository).save(any(Sala.class));
    }

    @Test
    void listarTodas_deveConverterCadaSalaEmDto() {
        Sala s1 = new Sala("Sala Azul", TipoSala.COLETIVA, 10);
        s1.setId(1L);
        Sala s2 = new Sala("Auditorio", TipoSala.AUDITORIO, 100);
        s2.setId(2L);
        when(salaRepository.findAll()).thenReturn(List.of(s1, s2));

        List<SalaResponseDTO> resultado = salaService.listarTodas();

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(SalaResponseDTO::nome)
                .containsExactly("Sala Azul", "Auditorio");
        assertThat(resultado).extracting(SalaResponseDTO::tipo)
                .containsExactly(TipoSala.COLETIVA, TipoSala.AUDITORIO);
    }

    @Test
    void listarTodas_quandoVazio_deveRetornarListaVazia() {
        when(salaRepository.findAll()).thenReturn(List.of());

        List<SalaResponseDTO> resultado = salaService.listarTodas();

        assertThat(resultado).isEmpty();
    }
}
