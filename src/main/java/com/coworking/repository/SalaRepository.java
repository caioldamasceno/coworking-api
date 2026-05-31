package com.coworking.repository;

import com.coworking.entity.Sala;
import com.coworking.enums.TipoSala;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaRepository extends JpaRepository<Sala, Long> {

    Page<Sala> findByTipo(TipoSala tipo, Pageable pageable);

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);
}
