package com.coworking.service;

import com.coworking.dto.request.SalaRequestDTO;
import com.coworking.dto.response.SalaResponseDTO;

import java.util.List;

public interface SalaService {

    SalaResponseDTO cadastrar(SalaRequestDTO dto);

    List<SalaResponseDTO> listarTodas();
}
