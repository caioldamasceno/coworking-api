package com.coworking.dto.request;

import com.coworking.enums.TipoSala;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SalaRequestDTO(

        @NotBlank(message = "O nome da sala é obrigatório")
        String nome,

        @NotNull(message = "O tipo da sala é obrigatório")
        TipoSala tipo,

        @Min(value = 3, message = "A capacidade deve ser maior que 2")
        @Max(value = 49, message = "A capacidade deve ser menor que 50")
        int capacidade
) {
}
