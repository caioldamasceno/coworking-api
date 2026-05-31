package com.coworking.controller;

import com.coworking.dto.request.SalaRequestDTO;
import com.coworking.dto.response.IntervaloLivreDTO;
import com.coworking.dto.response.SalaDisponivelDTO;
import com.coworking.dto.response.SalaResponseDTO;
import com.coworking.enums.TipoSala;
import com.coworking.exception.NomeDuplicadoException;
import com.coworking.exception.RecursoNaoEncontradoException;
import com.coworking.exception.ReservaVinculadaException;
import com.coworking.service.SalaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void cadastrar_comNomeDuplicado_deveRetornar409() throws Exception {
        SalaRequestDTO request = new SalaRequestDTO("Sala Azul", TipoSala.COLETIVA, 10);
        when(salaService.cadastrar(any(SalaRequestDTO.class)))
                .thenThrow(new NomeDuplicadoException("Já existe uma sala com o nome: Sala Azul"));

        mockMvc.perform(post("/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void cadastrar_quandoBancoViolaIntegridade_deveRetornar409() throws Exception {
        SalaRequestDTO request = new SalaRequestDTO("Sala Azul", TipoSala.COLETIVA, 10);
        when(salaService.cadastrar(any(SalaRequestDTO.class)))
                .thenThrow(new DataIntegrityViolationException("constraint violation"));

        mockMvc.perform(post("/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
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
    void listar_semFiltros_deveRetornar200ComAsSalasPaginadas() throws Exception {
        when(salaService.listarTodas(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(
                new SalaResponseDTO(1L, "Sala Azul", TipoSala.COLETIVA, 10))));

        mockMvc.perform(get("/salas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Sala Azul"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listar_comFiltroTipo_deveDelegarParaListarPorTipo() throws Exception {
        when(salaService.listarPorTipo(eq(TipoSala.INDIVIDUAL), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(
                new SalaResponseDTO(2L, "Sala Foco", TipoSala.INDIVIDUAL, 4))));

        mockMvc.perform(get("/salas").param("tipo", "INDIVIDUAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].tipo").value("INDIVIDUAL"));
    }

    @Test
    void disponibilidade_comData_deveRetornarSalasComHorariosLivres() throws Exception {
        when(salaService.listarDisponiveis(LocalDate.of(2026, 6, 1), null, null)).thenReturn(List.of(
                new SalaDisponivelDTO(1L, "Sala Azul", TipoSala.COLETIVA, 10,
                        List.of(new IntervaloLivreDTO(LocalTime.of(8, 0), LocalTime.of(22, 0))))));

        mockMvc.perform(get("/salas/disponibilidade").param("data", "2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].horariosLivres[0].inicio").exists())
                .andExpect(jsonPath("$[0].horariosLivres[0].fim").exists());
    }

    @Test
    void disponibilidade_comDataEIntervalo_deveDelegarComOsHorarios() throws Exception {
        when(salaService.listarDisponiveis(LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(10, 0)))
                .thenReturn(List.of(new SalaDisponivelDTO(1L, "Sala Azul", TipoSala.COLETIVA, 10, List.of())));

        mockMvc.perform(get("/salas/disponibilidade")
                        .param("data", "2026-06-01")
                        .param("horaInicio", "09:00")
                        .param("horaFim", "10:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void disponibilidade_semData_deveRetornar400() throws Exception {
        mockMvc.perform(get("/salas/disponibilidade"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void disponibilidade_comApenasHoraInicio_deveRetornar400() throws Exception {
        mockMvc.perform(get("/salas/disponibilidade")
                        .param("data", "2026-06-01")
                        .param("horaInicio", "09:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void atualizar_comDadosValidos_deveRetornar200() throws Exception {
        SalaRequestDTO request = new SalaRequestDTO("Sala Nova", TipoSala.INDIVIDUAL, 4);
        when(salaService.atualizar(eq(1L), any(SalaRequestDTO.class)))
                .thenReturn(new SalaResponseDTO(1L, "Sala Nova", TipoSala.INDIVIDUAL, 4));

        mockMvc.perform(put("/salas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Sala Nova"))
                .andExpect(jsonPath("$.tipo").value("INDIVIDUAL"));
    }

    @Test
    void atualizar_quandoSalaNaoExiste_deveRetornar404() throws Exception {
        when(salaService.atualizar(eq(99L), any(SalaRequestDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Sala não encontrada: 99"));

        mockMvc.perform(put("/salas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SalaRequestDTO("X", TipoSala.COLETIVA, 5))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void atualizar_comNomeVazio_deveRetornar400() throws Exception {
        mockMvc.perform(put("/salas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SalaRequestDTO("", TipoSala.COLETIVA, 10))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void excluir_comIdExistente_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/salas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void excluir_quandoSalaNaoExiste_deveRetornar404() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Sala não encontrada: 99"))
                .when(salaService).excluir(99L);

        mockMvc.perform(delete("/salas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void excluir_comReservaFutura_deveRetornar409() throws Exception {
        doThrow(new ReservaVinculadaException("Sala possui reservas futuras"))
                .when(salaService).excluir(1L);

        mockMvc.perform(delete("/salas/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
