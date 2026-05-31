package com.coworking.dto.request;

import com.coworking.domain.Funcionamento;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaRequestDTO(

        @NotNull(message = "O id da sala é obrigatório")
        Long salaId,

        @NotNull(message = "A data é obrigatória")
        @FutureOrPresent(message = "A data da reserva não pode ser no passado")
        LocalDate data,

        @NotNull(message = "A hora de início é obrigatória")
        LocalTime horaInicio,

        @NotNull(message = "A hora de fim é obrigatória")
        LocalTime horaFim,

        @NotBlank(message = "O responsável é obrigatório")
        String responsavel,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        String email
) {

    @AssertTrue(message = "A hora de início deve ser antes da hora de fim")
    public boolean isIntervaloValido() {
        return horaInicio == null || horaFim == null || horaInicio.isBefore(horaFim);
    }

    @AssertTrue(message = "O horário deve estar dentro do funcionamento do coworking (08:00 às 22:00)")
    public boolean isDentroDoFuncionamento() {
        if (horaInicio == null || horaFim == null) {
            return true;
        }
        return Funcionamento.contem(horaInicio, horaFim);
    }
}
