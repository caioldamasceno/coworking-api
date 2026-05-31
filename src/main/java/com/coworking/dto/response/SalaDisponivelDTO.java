package com.coworking.dto.response;

import com.coworking.enums.TipoSala;

import java.util.List;

public record SalaDisponivelDTO(
        Long id,
        String nome,
        TipoSala tipo,
        int capacidade,
        List<IntervaloLivreDTO> horariosLivres
) {
}
