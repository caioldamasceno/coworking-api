package com.coworking.service;

import com.coworking.dto.request.SalaRequestDTO;
import com.coworking.dto.response.SalaResponseDTO;
import com.coworking.entity.Sala;
import com.coworking.mapper.SalaMapper;
import com.coworking.repository.SalaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalaServiceImpl implements SalaService {

    private final SalaRepository salaRepository;
    private final SalaMapper salaMapper;

    public SalaServiceImpl(SalaRepository salaRepository, SalaMapper salaMapper) {
        this.salaRepository = salaRepository;
        this.salaMapper = salaMapper;
    }

    @Override
    public SalaResponseDTO cadastrar(SalaRequestDTO dto) {
        Sala sala = salaMapper.toEntity(dto);
        Sala salva = salaRepository.save(sala);
        return salaMapper.toResponse(salva);
    }

    @Override
    public List<SalaResponseDTO> listarTodas() {
        return salaRepository.findAll().stream()
                .map(salaMapper::toResponse)
                .toList();
    }
}
