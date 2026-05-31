package com.coworking.repository;

import com.coworking.entity.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SalaRepository extends JpaRepository<Sala, Long> {

    @Query("SELECT s FROM Sala s WHERE s.id NOT IN (SELECT r.sala.id FROM Reserva r WHERE r.data = :data)")
    List<Sala> findLivresPorData(@Param("data") LocalDate data);
}
