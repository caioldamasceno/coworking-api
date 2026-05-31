package com.coworking.controller;

import com.coworking.dto.request.ReservaRequestDTO;
import com.coworking.dto.response.ReservaResponseDTO;
import com.coworking.exception.ConflitoDeHorarioException;
import com.coworking.exception.RecursoNaoEncontradoException;
import com.coworking.service.ReservaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservaController.class)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservaService reservaService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private ReservaRequestDTO requisicaoValida() {
        return new ReservaRequestDTO(1L, LocalDate.of(2026, 6, 1),
                LocalTime.of(10, 0), LocalTime.of(11, 0), "Caio", "caio@email.com");
    }

    @Test
    void criar_comDadosValidos_deveRetornar201() throws Exception {
        ReservaResponseDTO response = new ReservaResponseDTO(99L, 1L, "Sala Azul",
                LocalDate.of(2026, 6, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), "Caio", "caio@email.com");
        when(reservaService.criar(any(ReservaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicaoValida())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.salaNome").value("Sala Azul"))
                .andExpect(jsonPath("$.responsavel").value("Caio"));
    }

    @Test
    void criar_quandoSalaNaoExiste_deveRetornar404() throws Exception {
        when(reservaService.criar(any(ReservaRequestDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Sala nao encontrada: 1"));

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicaoValida())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void criar_quandoHaConflito_deveRetornar409() throws Exception {
        when(reservaService.criar(any(ReservaRequestDTO.class)))
                .thenThrow(new ConflitoDeHorarioException("Ja existe reserva nesse horario"));

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicaoValida())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void criar_comResponsavelVazio_deveRetornar400() throws Exception {
        ReservaRequestDTO invalido = new ReservaRequestDTO(1L, LocalDate.of(2026, 6, 1),
                LocalTime.of(10, 0), LocalTime.of(11, 0), "", "caio@email.com");

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void criar_comHoraFimAntesDoInicio_deveRetornar400() throws Exception {
        ReservaRequestDTO invalido = new ReservaRequestDTO(1L, LocalDate.of(2026, 6, 1),
                LocalTime.of(11, 0), LocalTime.of(10, 0), "Caio", "caio@email.com");

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelar_comIdExistente_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/reservas/99"))
                .andExpect(status().isNoContent());
    }

    @Test
    void cancelar_quandoNaoExiste_deveRetornar404() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Reserva nao encontrada: 99"))
                .when(reservaService).cancelar(99L);

        mockMvc.perform(delete("/reservas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listarPorData_deveRetornar200ComAsReservas() throws Exception {
        ReservaResponseDTO reserva = new ReservaResponseDTO(1L, 1L, "Sala Azul",
                LocalDate.of(2026, 6, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), "Caio", "caio@email.com");
        when(reservaService.listarPorData(LocalDate.of(2026, 6, 1))).thenReturn(List.of(reserva));

        mockMvc.perform(get("/reservas").param("data", "2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].salaNome").value("Sala Azul"))
                .andExpect(jsonPath("$[0].responsavel").value("Caio"));
    }
}
