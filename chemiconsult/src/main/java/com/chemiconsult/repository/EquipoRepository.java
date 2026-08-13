package com.chemiconsult.repository;

import com.chemiconsult.entity.EquipoDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipoRepository extends JpaRepository<EquipoDE, Long> {
    List<EquipoDE> findAllByActivoTrueOrderByNombreAsc();
    List<EquipoDE> findAllByOrderByNombreAsc();
}
