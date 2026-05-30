package com.coworking.mapper;

import com.coworking.dto.request.SalaRequestDTO;
import com.coworking.dto.response.SalaResponseDTO;
import com.coworking.entity.Sala;
import com.coworking.enums.TipoSala;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SalaMapperTest {

    private final SalaMapper salaMapper = new SalaMapper();

    @Test
    void toEntity_deveMapearOsCampos() {
        SalaRequestDTO dto = new SalaRequestDTO("Sala Azul", TipoSala.COLETIVA, 10);

        Sala sala = salaMapper.toEntity(dto);

        assertThat(sala.getNome()).isEqualTo("Sala Azul");
        assertThat(sala.getTipo()).isEqualTo(TipoSala.COLETIVA);
        assertThat(sala.getCapacidade()).isEqualTo(10);
    }

    @Test
    void toResponse_deveMapearOsCampos() {
        Sala sala = new Sala("Auditorio", TipoSala.AUDITORIO, 40);
        sala.setId(7L);

        SalaResponseDTO dto = salaMapper.toResponse(sala);

        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.nome()).isEqualTo("Auditorio");
        assertThat(dto.tipo()).isEqualTo(TipoSala.AUDITORIO);
        assertThat(dto.capacidade()).isEqualTo(40);
    }
}
