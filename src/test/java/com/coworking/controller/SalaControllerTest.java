package com.coworking.controller;

import com.coworking.dto.request.SalaRequestDTO;
import com.coworking.dto.response.SalaResponseDTO;
import com.coworking.enums.TipoSala;
import com.coworking.service.SalaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SalaController.class)
class SalaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SalaService salaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void cadastrar_comDadosValidos_deveRetornar201() throws Exception {
        SalaRequestDTO request = new SalaRequestDTO("Sala Azul", TipoSala.COLETIVA, 10);
        SalaResponseDTO response = new SalaResponseDTO(1L, "Sala Azul", TipoSala.COLETIVA, 10);
        when(salaService.cadastrar(any(SalaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Sala Azul"))
                .andExpect(jsonPath("$.tipo").value("COLETIVA"));
    }

    @Test
    void cadastrar_comNomeVazio_deveRetornar400() throws Exception {
        SalaRequestDTO invalido = new SalaRequestDTO("", TipoSala.COLETIVA, 10);

        mockMvc.perform(post("/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 50, 0, -1, 100})
    void cadastrar_comCapacidadeForaDoIntervalo_deveRetornar400(int capacidade) throws Exception {
        SalaRequestDTO invalido = new SalaRequestDTO("Sala X", TipoSala.COLETIVA, capacidade);

        mockMvc.perform(post("/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 49})
    void cadastrar_naCapacidadeLimiteValida_deveRetornar201(int capacidade) throws Exception {
        SalaRequestDTO request = new SalaRequestDTO("Sala X", TipoSala.COLETIVA, capacidade);
        when(salaService.cadastrar(any(SalaRequestDTO.class)))
                .thenReturn(new SalaResponseDTO(1L, "Sala X", TipoSala.COLETIVA, capacidade));

        mockMvc.perform(post("/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void listar_deveRetornar200ComAsSalas() throws Exception {
        when(salaService.listarTodas()).thenReturn(List.of(
                new SalaResponseDTO(1L, "Sala Azul", TipoSala.COLETIVA, 10)));

        mockMvc.perform(get("/salas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Sala Azul"));
    }

    @Test
    void listarLivres_deveRetornar200ComAsSalasLivres() throws Exception {
        when(salaService.listarLivresPorData(LocalDate.of(2026, 6, 1)))
                .thenReturn(List.of(new SalaResponseDTO(1L, "Sala Azul", TipoSala.COLETIVA, 10)));

        mockMvc.perform(get("/salas/livres").param("data", "2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Sala Azul"));
    }
}
