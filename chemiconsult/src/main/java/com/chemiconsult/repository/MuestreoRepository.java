package com.chemiconsult.repository;

import com.chemiconsult.entity.MuestreoDE;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MuestreoRepository extends JpaRepository<MuestreoDE, Long> {

    List<MuestreoDE> findByFechaHoraBetween(LocalDateTime start, LocalDateTime end);

    List<MuestreoDE> findByCliente_Id(Long clienteId);
}
