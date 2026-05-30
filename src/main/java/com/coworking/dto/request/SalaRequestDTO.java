package com.coworking.dto.request;

import com.coworking.enums.TipoSala;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SalaRequestDTO(

        @NotBlank(message = "O nome da sala é obrigatório")
        String nome,

        @NotNull(message = "O tipo da sala é obrigatório")
        TipoSala tipo,

        @Positive(message = "A capacidade deve ser maior que zero")
        int capacidade
) {
}
