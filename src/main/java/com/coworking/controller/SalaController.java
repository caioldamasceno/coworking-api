package com.coworking.controller;

import com.coworking.dto.request.SalaRequestDTO;
import com.coworking.dto.response.SalaResponseDTO;
import com.coworking.service.SalaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @PostMapping
    public ResponseEntity<SalaResponseDTO> cadastrar(@Valid @RequestBody SalaRequestDTO dto) {
        SalaResponseDTO criada = salaService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<SalaResponseDTO>> listar() {
        return ResponseEntity.ok(salaService.listarTodas());
    }

    @GetMapping("/livres")
    public ResponseEntity<List<SalaResponseDTO>> listarLivres(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(salaService.listarLivresPorData(data));
    }
}
