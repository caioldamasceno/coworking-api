package com.coworking.service;

import com.coworking.dto.request.ReservaRequestDTO;
import com.coworking.dto.response.ReservaResponseDTO;

public interface ReservaService {

    ReservaResponseDTO criar(ReservaRequestDTO dto);

    void cancelar(Long id);
}
