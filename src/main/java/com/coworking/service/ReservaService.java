package com.coworking.service;

import com.coworking.dto.request.ReservaRequestDTO;
import com.coworking.dto.response.ReservaResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReservaService {

    ReservaResponseDTO criar(ReservaRequestDTO dto);

    void cancelar(Long id);

    List<ReservaResponseDTO> listarPorData(LocalDate data);
}
