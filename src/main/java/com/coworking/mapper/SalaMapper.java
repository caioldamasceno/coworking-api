package com.coworking.mapper;

import com.coworking.dto.request.SalaRequestDTO;
import com.coworking.dto.response.SalaResponseDTO;
import com.coworking.entity.Sala;
import org.springframework.stereotype.Component;

@Component
public class SalaMapper {

    public Sala toEntity(SalaRequestDTO dto) {
        return new Sala(dto.nome(), dto.tipo(), dto.capacidade());
    }

    public SalaResponseDTO toResponse(Sala sala) {
        return new SalaResponseDTO(sala.getId(), sala.getNome(), sala.getTipo(), sala.getCapacidade());
    }
}
