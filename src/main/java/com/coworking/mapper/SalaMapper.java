package com.coworking.mapper;

import com.coworking.domain.IntervaloHorario;
import com.coworking.dto.request.SalaRequestDTO;
import com.coworking.dto.response.IntervaloLivreDTO;
import com.coworking.dto.response.SalaDisponivelDTO;
import com.coworking.dto.response.SalaResponseDTO;
import com.coworking.entity.Sala;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SalaMapper {

    public Sala toEntity(SalaRequestDTO dto) {
        return new Sala(dto.nome(), dto.tipo(), dto.capacidade());
    }

    public void aplicar(SalaRequestDTO dto, Sala sala) {
        sala.setNome(dto.nome());
        sala.setTipo(dto.tipo());
        sala.setCapacidade(dto.capacidade());
    }

    public SalaResponseDTO toResponse(Sala sala) {
        return new SalaResponseDTO(sala.getId(), sala.getNome(), sala.getTipo(), sala.getCapacidade());
    }

    public SalaDisponivelDTO toDisponivel(Sala sala, List<IntervaloHorario> horariosLivres) {
        List<IntervaloLivreDTO> intervalos = horariosLivres.stream()
                .map(intervalo -> new IntervaloLivreDTO(intervalo.getInicio(), intervalo.getFim()))
                .toList();
        return new SalaDisponivelDTO(sala.getId(), sala.getNome(), sala.getTipo(), sala.getCapacidade(), intervalos);
    }
}
