package com.coworking.service;

import com.coworking.domain.Funcionamento;
import com.coworking.domain.IntervaloHorario;
import com.coworking.dto.request.SalaRequestDTO;
import com.coworking.dto.response.SalaDisponivelDTO;
import com.coworking.dto.response.SalaResponseDTO;
import com.coworking.entity.Sala;
import com.coworking.enums.TipoSala;
import com.coworking.exception.NomeDuplicadoException;
import com.coworking.exception.RecursoNaoEncontradoException;
import com.coworking.exception.ReservaVinculadaException;
import com.coworking.mapper.SalaMapper;
import com.coworking.repository.ReservaRepository;
import com.coworking.repository.SalaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SalaServiceImpl implements SalaService {

    private final SalaRepository salaRepository;
    private final ReservaRepository reservaRepository;
    private final SalaMapper salaMapper;

    public SalaServiceImpl(SalaRepository salaRepository, ReservaRepository reservaRepository, SalaMapper salaMapper) {
        this.salaRepository = salaRepository;
        this.reservaRepository = reservaRepository;
        this.salaMapper = salaMapper;
    }

    @Override
    @Transactional
    public SalaResponseDTO cadastrar(SalaRequestDTO dto) {
        if (salaRepository.existsByNome(dto.nome())) {
            throw new NomeDuplicadoException("Já existe uma sala com o nome: " + dto.nome());
        }
        Sala sala = salaMapper.toEntity(dto);
        Sala salva = salaRepository.save(sala);
        return salaMapper.toResponse(salva);
    }

    @Override
    @Transactional
    public SalaResponseDTO atualizar(Long id, SalaRequestDTO dto) {
        Sala sala = salaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sala não encontrada: " + id));
        if (salaRepository.existsByNomeAndIdNot(dto.nome(), id)) {
            throw new NomeDuplicadoException("Já existe uma sala com o nome: " + dto.nome());
        }
        salaMapper.aplicar(dto, sala);
        Sala salva = salaRepository.save(sala);
        return salaMapper.toResponse(salva);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        if (!salaRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Sala não encontrada: " + id);
        }
        if (reservaRepository.existsBySalaIdAndDataGreaterThanEqual(id, LocalDate.now())) {
            throw new ReservaVinculadaException("Não é possível excluir a sala " + id + ": existem reservas futuras vinculadas");
        }
        salaRepository.deleteById(id);
    }

    @Override
    public Page<SalaResponseDTO> listarTodas(Pageable pageable) {
        return salaRepository.findAll(pageable).map(salaMapper::toResponse);
    }

    @Override
    public Page<SalaResponseDTO> listarPorTipo(TipoSala tipo, Pageable pageable) {
        return salaRepository.findByTipo(tipo, pageable).map(salaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaDisponivelDTO> listarDisponiveis(LocalDate data, LocalTime horaInicio, LocalTime horaFim) {
        IntervaloHorario funcionamento = Funcionamento.intervalo();
        IntervaloHorario intervaloDesejado = intervaloDesejado(horaInicio, horaFim);
        Map<Long, List<IntervaloHorario>> ocupadosPorSala = ocupadosPorSala(data);

        return salaRepository.findAll().stream()
                .map(sala -> {
                    List<IntervaloHorario> ocupados = ocupadosPorSala.getOrDefault(sala.getId(), List.of());
                    return new Disponibilidade(sala, ocupados, funcionamento.subtrair(ocupados));
                })
                .filter(disp -> estaDisponivel(disp, intervaloDesejado))
                .map(disp -> salaMapper.toDisponivel(disp.sala(), disp.livres()))
                .toList();
    }

    private static IntervaloHorario intervaloDesejado(LocalTime horaInicio, LocalTime horaFim) {
        return (horaInicio != null && horaFim != null) ? new IntervaloHorario(horaInicio, horaFim) : null;
    }

    private Map<Long, List<IntervaloHorario>> ocupadosPorSala(LocalDate data) {
        return reservaRepository.findByDataOrderByHoraInicioAsc(data).stream()
                .collect(Collectors.groupingBy(
                        reserva -> reserva.getSala().getId(),
                        Collectors.mapping(
                                reserva -> new IntervaloHorario(reserva.getHoraInicio(), reserva.getHoraFim()),
                                Collectors.toList())));
    }

    private static boolean estaDisponivel(Disponibilidade disp, IntervaloHorario intervaloDesejado) {
        if (intervaloDesejado != null) {
            return disp.ocupados().stream().noneMatch(intervaloDesejado::sobrepoe);
        }
        return !disp.livres().isEmpty();
    }

    private record Disponibilidade(Sala sala, List<IntervaloHorario> ocupados, List<IntervaloHorario> livres) {
    }
}
