package com.coworking.service;

import com.coworking.dto.request.SalaRequestDTO;
import com.coworking.dto.response.IntervaloLivreDTO;
import com.coworking.dto.response.SalaDisponivelDTO;
import com.coworking.dto.response.SalaResponseDTO;
import com.coworking.entity.Reserva;
import com.coworking.entity.Sala;
import com.coworking.enums.TipoSala;
import com.coworking.exception.NomeDuplicadoException;
import com.coworking.exception.RecursoNaoEncontradoException;
import com.coworking.exception.ReservaVinculadaException;
import com.coworking.mapper.SalaMapper;
import com.coworking.repository.ReservaRepository;
import com.coworking.repository.SalaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalaServiceTest {

    @Mock
    private SalaRepository salaRepository;

    @Mock
    private ReservaRepository reservaRepository;

    private SalaService salaService;

    @BeforeEach
    void setUp() {
        salaService = new SalaServiceImpl(salaRepository, reservaRepository, new SalaMapper());
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
    void listarTodas_deveConverterCadaSalaEmDtoPreservandoAPaginacao() {
        Sala s1 = new Sala("Sala Azul", TipoSala.COLETIVA, 10);
        s1.setId(1L);
        Sala s2 = new Sala("Auditorio", TipoSala.AUDITORIO, 40);
        s2.setId(2L);
        Pageable pageable = PageRequest.of(0, 20);
        when(salaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(s1, s2), pageable, 2));

        Page<SalaResponseDTO> resultado = salaService.listarTodas(pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(2);
        assertThat(resultado.getContent()).extracting(SalaResponseDTO::nome)
                .containsExactly("Sala Azul", "Auditorio");
        assertThat(resultado.getContent()).extracting(SalaResponseDTO::tipo)
                .containsExactly(TipoSala.COLETIVA, TipoSala.AUDITORIO);
    }

    @Test
    void listarTodas_quandoVazio_deveRetornarPaginaVazia() {
        Pageable pageable = PageRequest.of(0, 20);
        when(salaRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

        assertThat(salaService.listarTodas(pageable).getContent()).isEmpty();
    }

    @Test
    void listarPorTipo_deveConverterApenasAsSalasDoTipoInformado() {
        Sala individual = new Sala("Sala Foco", TipoSala.INDIVIDUAL, 4);
        individual.setId(1L);
        Pageable pageable = PageRequest.of(0, 20);
        when(salaRepository.findByTipo(TipoSala.INDIVIDUAL, pageable))
                .thenReturn(new PageImpl<>(List.of(individual), pageable, 1));

        Page<SalaResponseDTO> resultado = salaService.listarPorTipo(TipoSala.INDIVIDUAL, pageable);

        assertThat(resultado.getContent()).extracting(SalaResponseDTO::id).containsExactly(1L);
        assertThat(resultado.getContent()).extracting(SalaResponseDTO::tipo).containsExactly(TipoSala.INDIVIDUAL);
    }

    @Test
    void listarPorTipo_quandoNaoHaSalasDoTipo_deveRetornarPaginaVazia() {
        Pageable pageable = PageRequest.of(0, 20);
        when(salaRepository.findByTipo(TipoSala.AUDITORIO, pageable)).thenReturn(Page.empty(pageable));

        assertThat(salaService.listarPorTipo(TipoSala.AUDITORIO, pageable).getContent()).isEmpty();
    }

    @Test
    void atualizar_comIdExistente_deveAtualizarCamposERetornarDto() {
        Sala existente = new Sala("Sala Antiga", TipoSala.COLETIVA, 10);
        existente.setId(1L);
        when(salaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(salaRepository.save(any(Sala.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        SalaResponseDTO resposta = salaService.atualizar(1L, new SalaRequestDTO("Sala Nova", TipoSala.INDIVIDUAL, 4));

        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.nome()).isEqualTo("Sala Nova");
        assertThat(resposta.tipo()).isEqualTo(TipoSala.INDIVIDUAL);
        assertThat(resposta.capacidade()).isEqualTo(4);
        verify(salaRepository).save(existente);
    }

    @Test
    void atualizar_comIdInexistente_deveLancarNaoEncontrado() {
        when(salaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> salaService.atualizar(99L, new SalaRequestDTO("X", TipoSala.COLETIVA, 5)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(salaRepository, never()).save(any());
    }

    @Test
    void cadastrar_comNomeDuplicado_deveLancarConflito() {
        when(salaRepository.existsByNome("Sala Azul")).thenReturn(true);

        assertThatThrownBy(() -> salaService.cadastrar(new SalaRequestDTO("Sala Azul", TipoSala.COLETIVA, 10)))
                .isInstanceOf(NomeDuplicadoException.class);
        verify(salaRepository, never()).save(any());
    }

    @Test
    void atualizar_comNomeDuplicadoEmOutraSala_deveLancarConflito() {
        Sala existente = new Sala("Sala Antiga", TipoSala.COLETIVA, 10);
        existente.setId(1L);
        when(salaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(salaRepository.existsByNomeAndIdNot("Sala Nova", 1L)).thenReturn(true);

        assertThatThrownBy(() -> salaService.atualizar(1L, new SalaRequestDTO("Sala Nova", TipoSala.INDIVIDUAL, 4)))
                .isInstanceOf(NomeDuplicadoException.class);
        verify(salaRepository, never()).save(any());
    }

    @Test
    void excluir_quandoSalaNaoExiste_deveLancarNaoEncontrado() {
        when(salaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> salaService.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(salaRepository, never()).deleteById(any());
    }

    @Test
    void excluir_comReservaFutura_deveLancarConflito() {
        when(salaRepository.existsById(1L)).thenReturn(true);
        when(reservaRepository.existsBySalaIdAndDataGreaterThanEqual(eq(1L), any(LocalDate.class))).thenReturn(true);

        assertThatThrownBy(() -> salaService.excluir(1L))
                .isInstanceOf(ReservaVinculadaException.class);
        verify(salaRepository, never()).deleteById(any());
    }

    @Test
    void excluir_semReservaFutura_deveRemoverASala() {
        when(salaRepository.existsById(1L)).thenReturn(true);
        when(reservaRepository.existsBySalaIdAndDataGreaterThanEqual(eq(1L), any(LocalDate.class))).thenReturn(false);

        salaService.excluir(1L);

        verify(salaRepository).deleteById(1L);
    }

    @Test
    void listarDisponiveis_semHorario_eSemReservas_deveRetornarJanelaInteiraDeFuncionamento() {
        LocalDate dia = LocalDate.of(2026, 6, 1);
        Sala sala = new Sala("Sala A", TipoSala.COLETIVA, 10);
        sala.setId(1L);
        when(salaRepository.findAll()).thenReturn(List.of(sala));
        when(reservaRepository.findByDataOrderByHoraInicioAsc(dia)).thenReturn(List.of());

        List<SalaDisponivelDTO> resultado = salaService.listarDisponiveis(dia, null, null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(1L);
        assertThat(resultado.get(0).horariosLivres())
                .extracting(IntervaloLivreDTO::inicio, IntervaloLivreDTO::fim)
                .containsExactly(tuple(LocalTime.of(8, 0), LocalTime.of(22, 0)));
    }

    @Test
    void listarDisponiveis_semHorario_comReservaNoMeio_deveRetornarApenasOsHorariosLivres() {
        LocalDate dia = LocalDate.of(2026, 6, 1);
        Sala sala = new Sala("Sala A", TipoSala.COLETIVA, 10);
        sala.setId(1L);
        Reserva reserva = new Reserva(sala, dia, LocalTime.of(10, 0), LocalTime.of(11, 0), "Ana", "ana@email.com");
        when(salaRepository.findAll()).thenReturn(List.of(sala));
        when(reservaRepository.findByDataOrderByHoraInicioAsc(dia)).thenReturn(List.of(reserva));

        List<SalaDisponivelDTO> resultado = salaService.listarDisponiveis(dia, null, null);

        assertThat(resultado.get(0).horariosLivres())
                .extracting(IntervaloLivreDTO::inicio, IntervaloLivreDTO::fim)
                .containsExactly(
                        tuple(LocalTime.of(8, 0), LocalTime.of(10, 0)),
                        tuple(LocalTime.of(11, 0), LocalTime.of(22, 0)));
    }

    @Test
    void listarDisponiveis_semHorario_salaTotalmenteOcupada_naoDeveAparecer() {
        LocalDate dia = LocalDate.of(2026, 6, 1);
        Sala ocupada = new Sala("Ocupada", TipoSala.COLETIVA, 10);
        ocupada.setId(1L);
        Sala livre = new Sala("Livre", TipoSala.INDIVIDUAL, 4);
        livre.setId(2L);
        Reserva diaInteiro = new Reserva(ocupada, dia, LocalTime.of(8, 0), LocalTime.of(22, 0), "Ana", "ana@email.com");
        when(salaRepository.findAll()).thenReturn(List.of(ocupada, livre));
        when(reservaRepository.findByDataOrderByHoraInicioAsc(dia)).thenReturn(List.of(diaInteiro));

        List<SalaDisponivelDTO> resultado = salaService.listarDisponiveis(dia, null, null);

        assertThat(resultado).extracting(SalaDisponivelDTO::id).containsExactly(2L);
    }

    @Test
    void listarDisponiveis_comIntervalo_deveRetornarApenasSalasLivresNaqueleIntervalo() {
        LocalDate dia = LocalDate.of(2026, 6, 1);
        Sala ocupada = new Sala("Ocupada", TipoSala.COLETIVA, 10);
        ocupada.setId(1L);
        Sala livre = new Sala("Livre", TipoSala.INDIVIDUAL, 4);
        livre.setId(2L);
        Reserva reserva = new Reserva(ocupada, dia, LocalTime.of(10, 0), LocalTime.of(11, 0), "Ana", "ana@email.com");
        when(salaRepository.findAll()).thenReturn(List.of(ocupada, livre));
        when(reservaRepository.findByDataOrderByHoraInicioAsc(dia)).thenReturn(List.of(reserva));

        List<SalaDisponivelDTO> resultado = salaService.listarDisponiveis(dia, LocalTime.of(10, 0), LocalTime.of(10, 30));

        assertThat(resultado).extracting(SalaDisponivelDTO::id).containsExactly(2L);
    }
}
