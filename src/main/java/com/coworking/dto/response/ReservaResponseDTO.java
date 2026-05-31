package com.coworking.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaResponseDTO(
        Long id,
        Long salaId,
        String salaNome,
        LocalDate data,
        LocalTime horaInicio,
        LocalTime horaFim,
        String responsavel,
        String email
) {
}
