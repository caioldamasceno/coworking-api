package com.coworking.service;

import com.coworking.domain.IntervaloHorario;
import com.coworking.dto.request.ReservaRequestDTO;
import com.coworking.dto.response.ReservaResponseDTO;
import com.coworking.entity.Reserva;
import com.coworking.entity.Sala;
import com.coworking.exception.ConflitoDeHorarioException;
import com.coworking.exception.RecursoNaoEncontradoException;
import com.coworking.mapper.ReservaMapper;
import com.coworking.repository.ReservaRepository;
import com.coworking.repository.SalaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final SalaRepository salaRepository;
    private final ReservaMapper reservaMapper;

    public ReservaServiceImpl(ReservaRepository reservaRepository, SalaRepository salaRepository, ReservaMapper reservaMapper) {
        this.reservaRepository = reservaRepository;
        this.salaRepository = salaRepository;
        this.reservaMapper = reservaMapper;
    }

    @Override
    @Transactional
    public ReservaResponseDTO criar(ReservaRequestDTO dto) {
        Sala sala = salaRepository.findById(dto.salaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sala não encontrada: " + dto.salaId()));

        IntervaloHorario novo = new IntervaloHorario(dto.horaInicio(), dto.horaFim());
        boolean conflita = reservaRepository.findBySalaIdAndData(dto.salaId(), dto.data()).stream()
                .map(reserva -> new IntervaloHorario(reserva.getHoraInicio(), reserva.getHoraFim()))
                .anyMatch(novo::sobrepoe);

        if (conflita) {
            throw new ConflitoDeHorarioException("Já existe uma reserva nesse horário para a sala " + dto.salaId());
        }

        Reserva reserva = reservaMapper.toEntity(dto, sala);
        Reserva salva = reservaRepository.save(reserva);
        return reservaMapper.toResponse(salva);
    }

    @Override
    @Transactional
    public void cancelar(Long id) {
        if (!reservaRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Reserva não encontrada: " + id);
        }
        reservaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarPorData(LocalDate data) {
        return reservaRepository.findByDataOrderByHoraInicioAsc(data).stream()
                .map(reservaMapper::toResponse)
                .toList();
    }
}
