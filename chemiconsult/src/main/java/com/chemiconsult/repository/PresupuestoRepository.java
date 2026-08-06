package com.chemiconsult.repository;

import com.chemiconsult.entity.PresupuestoDE;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PresupuestoRepository extends JpaRepository<PresupuestoDE, Long> {
    List<PresupuestoDE> findAllByOrderByNumeroDesc();
}
