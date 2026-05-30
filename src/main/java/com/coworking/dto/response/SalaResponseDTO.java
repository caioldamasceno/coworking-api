package com.coworking.dto.response;

import com.coworking.enums.TipoSala;

public record SalaResponseDTO(
        Long id,
        String nome,
        TipoSala tipo,
        int capacidade
) {
}
