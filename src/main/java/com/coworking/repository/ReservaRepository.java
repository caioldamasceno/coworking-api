package com.coworking.repository;

import com.coworking.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findBySalaIdAndData(Long salaId, LocalDate data);

    List<Reserva> findByDataOrderByHoraInicioAsc(LocalDate data);

    boolean existsBySalaIdAndDataGreaterThanEqual(Long salaId, LocalDate data);
}
