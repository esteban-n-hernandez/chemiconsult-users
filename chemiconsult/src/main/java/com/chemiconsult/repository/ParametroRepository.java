package com.chemiconsult.repository;

import com.chemiconsult.entity.ParametroDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParametroRepository extends JpaRepository<ParametroDE, Long> {
    // Solo activos — para el frontend
    List<ParametroDE> findByActivoTrue();

    // Validar duplicado por nombre
    boolean existsByNombreIgnoreCase(String nombre);

    @Query("SELECT p FROM ParametroDE p LEFT JOIN FETCH p.responsable WHERE p.activo = true")
    List<ParametroDE> findByActivoTrueWithResponsable();

    @Query("SELECT p FROM ParametroDE p LEFT JOIN FETCH p.responsable")
    List<ParametroDE> findAllWithResponsable();
}
