package com.coworking.mapper;

import com.coworking.dto.request.ReservaRequestDTO;
import com.coworking.dto.response.ReservaResponseDTO;
import com.coworking.entity.Reserva;
import com.coworking.entity.Sala;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {

    public Reserva toEntity(ReservaRequestDTO dto, Sala sala) {
        return new Reserva(sala, dto.data(), dto.horaInicio(), dto.horaFim(), dto.responsavel(), dto.email());
    }

    public ReservaResponseDTO toResponse(Reserva reserva) {
        return new ReservaResponseDTO(
                reserva.getId(),
                reserva.getSala().getId(),
                reserva.getSala().getNome(),
                reserva.getData(),
                reserva.getHoraInicio(),
                reserva.getHoraFim(),
                reserva.getResponsavel(),
                reserva.getEmail());
    }
}
