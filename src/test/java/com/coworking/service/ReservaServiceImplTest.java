package com.coworking.service;

import com.coworking.dto.request.ReservaRequestDTO;
import com.coworking.dto.response.ReservaResponseDTO;
import com.coworking.entity.Reserva;
import com.coworking.entity.Sala;
import com.coworking.enums.TipoSala;
import com.coworking.exception.ConflitoDeHorarioException;
import com.coworking.exception.RecursoNaoEncontradoException;
import com.coworking.mapper.ReservaMapper;
import com.coworking.repository.ReservaRepository;
import com.coworking.repository.SalaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private SalaRepository salaRepository;

    private ReservaService reservaService;

    private Sala sala;

    private static final LocalDate DIA = LocalDate.of(2026, 6, 1);

    @BeforeEach
    void setUp() {
        reservaService = new ReservaServiceImpl(reservaRepository, salaRepository, new ReservaMapper());
        sala = new Sala("Sala Azul", TipoSala.COLETIVA, 10);
        sala.setId(1L);
    }

    private ReservaRequestDTO request(String inicio, String fim) {
        return new ReservaRequestDTO(1L, DIA, LocalTime.parse(inicio), LocalTime.parse(fim), "Caio", "caio@email.com");
    }

    @Test
    void criar_comSalaInexistente_deveLancarNaoEncontrado() {
        when(salaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.criar(request("10:00", "11:00")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void criar_comHorarioConflitante_deveLancarConflito() {
        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        Reserva existente = new Reserva(sala, DIA, LocalTime.of(10, 30), LocalTime.of(11, 30), "Ana", "ana@email.com");
        when(reservaRepository.findBySalaIdAndData(1L, DIA)).thenReturn(List.of(existente));

        assertThatThrownBy(() -> reservaService.criar(request("10:00", "11:00")))
                .isInstanceOf(ConflitoDeHorarioException.class);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void criar_semConflito_deveSalvarERetornar() {
        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(reservaRepository.findBySalaIdAndData(1L, DIA)).thenReturn(List.of());
        Reserva salva = new Reserva(sala, DIA, LocalTime.of(10, 0), LocalTime.of(11, 0), "Caio", "caio@email.com");
        salva.setId(99L);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(salva);

        ReservaResponseDTO resposta = reservaService.criar(request("10:00", "11:00"));

        assertThat(resposta.id()).isEqualTo(99L);
        assertThat(resposta.salaId()).isEqualTo(1L);
        assertThat(resposta.salaNome()).isEqualTo("Sala Azul");
        assertThat(resposta.responsavel()).isEqualTo("Caio");
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void criar_comReservaEmOutroHorario_naoDeveConflitar() {
        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        Reserva existente = new Reserva(sala, DIA, LocalTime.of(8, 0), LocalTime.of(9, 0), "Ana", "ana@email.com");
        when(reservaRepository.findBySalaIdAndData(1L, DIA)).thenReturn(List.of(existente));
        Reserva salva = new Reserva(sala, DIA, LocalTime.of(10, 0), LocalTime.of(11, 0), "Caio", "caio@email.com");
        salva.setId(5L);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(salva);

        ReservaResponseDTO resposta = reservaService.criar(request("10:00", "11:00"));

        assertThat(resposta.id()).isEqualTo(5L);
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void cancelar_comIdExistente_deveDeletar() {
        when(reservaRepository.existsById(99L)).thenReturn(true);

        reservaService.cancelar(99L);

        verify(reservaRepository).deleteById(99L);
    }

    @Test
    void cancelar_comIdInexistente_deveLancarNaoEncontrado() {
        when(reservaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> reservaService.cancelar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(reservaRepository, never()).deleteById(any());
    }

    @Test
    void listarPorData_deveRetornarReservasDoDiaConvertidas() {
        Reserva r1 = new Reserva(sala, DIA, LocalTime.of(9, 0), LocalTime.of(10, 0), "Ana", "ana@email.com");
        r1.setId(1L);
        Reserva r2 = new Reserva(sala, DIA, LocalTime.of(11, 0), LocalTime.of(12, 0), "Caio", "caio@email.com");
        r2.setId(2L);
        when(reservaRepository.findByDataOrderByHoraInicioAsc(DIA)).thenReturn(List.of(r1, r2));

        List<ReservaResponseDTO> resultado = reservaService.listarPorData(DIA);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(ReservaResponseDTO::id).containsExactly(1L, 2L);
        assertThat(resultado).extracting(ReservaResponseDTO::responsavel).containsExactly("Ana", "Caio");
    }

    @Test
    void listarPorData_quandoNaoHaReservas_deveRetornarListaVazia() {
        when(reservaRepository.findByDataOrderByHoraInicioAsc(DIA)).thenReturn(List.of());

        assertThat(reservaService.listarPorData(DIA)).isEmpty();
    }
}
