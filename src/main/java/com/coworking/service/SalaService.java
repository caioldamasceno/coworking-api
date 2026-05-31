package com.coworking.service;

import com.coworking.dto.request.SalaRequestDTO;
import com.coworking.dto.response.SalaDisponivelDTO;
import com.coworking.dto.response.SalaResponseDTO;
import com.coworking.enums.TipoSala;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SalaService {

    SalaResponseDTO cadastrar(SalaRequestDTO dto);

    SalaResponseDTO atualizar(Long id, SalaRequestDTO dto);

    void excluir(Long id);

    Page<SalaResponseDTO> listarTodas(Pageable pageable);

    Page<SalaResponseDTO> listarPorTipo(TipoSala tipo, Pageable pageable);

    List<SalaDisponivelDTO> listarDisponiveis(LocalDate data, LocalTime horaInicio, LocalTime horaFim);
}
