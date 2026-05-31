package com.coworking.controller;

import com.coworking.dto.request.SalaRequestDTO;
import com.coworking.dto.response.SalaDisponivelDTO;
import com.coworking.dto.response.SalaResponseDTO;
import com.coworking.enums.TipoSala;
import com.coworking.exception.RequisicaoInvalidaException;
import com.coworking.service.SalaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/salas")
@Tag(name = "Salas", description = "Cadastro e consulta de salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @PostMapping
    @Operation(summary = "Cadastra uma nova sala")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sala criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    public ResponseEntity<SalaResponseDTO> cadastrar(@Valid @RequestBody SalaRequestDTO dto) {
        SalaResponseDTO criada = salaService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma sala existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sala atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Sala nao encontrada")
    })
    public ResponseEntity<SalaResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody SalaRequestDTO dto) {
        return ResponseEntity.ok(salaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma sala, desde que nao possua reservas futuras vinculadas")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sala excluida"),
            @ApiResponse(responseCode = "404", description = "Sala nao encontrada"),
            @ApiResponse(responseCode = "409", description = "Sala possui reservas futuras vinculadas")
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        salaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Lista as salas cadastradas (paginado), com filtro opcional por tipo")
    @ApiResponse(responseCode = "200", description = "Pagina de salas")
    public ResponseEntity<Page<SalaResponseDTO>> listar(
            @RequestParam(required = false) TipoSala tipo,
            Pageable pageable) {
        Page<SalaResponseDTO> salas = (tipo != null)
                ? salaService.listarPorTipo(tipo, pageable)
                : salaService.listarTodas(pageable);
        return ResponseEntity.ok(salas);
    }

    @GetMapping("/disponibilidade")
    @Operation(summary = "Lista as salas com seus horarios livres em uma data",
            description = "Para cada sala com disponibilidade, retorna os intervalos livres dentro do funcionamento "
                    + "(08:00-22:00). Informando 'horaInicio' e 'horaFim', retorna apenas as salas livres naquele intervalo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Salas disponiveis na data"),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos (ex.: 'data' ausente)")
    })
    public ResponseEntity<List<SalaDisponivelDTO>> disponibilidade(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFim) {
        if ((horaInicio == null) != (horaFim == null)) {
            throw new RequisicaoInvalidaException("Informe 'horaInicio' e 'horaFim' juntos ou nenhum dos dois");
        }
        return ResponseEntity.ok(salaService.listarDisponiveis(data, horaInicio, horaFim));
    }
}
