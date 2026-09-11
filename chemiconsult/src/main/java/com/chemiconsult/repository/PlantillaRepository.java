package com.chemiconsult.repository;

import com.chemiconsult.entity.PlantillaDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantillaRepository extends JpaRepository<PlantillaDE, Long> {
    List<PlantillaDE> findByActivoTrue();

    @Query("SELECT DISTINCT p FROM PlantillaDE p LEFT JOIN FETCH p.parametros WHERE p.activo = true")
    List<PlantillaDE> findByActivoTrueWithParametros();

    @Query("SELECT DISTINCT p FROM PlantillaDE p LEFT JOIN FETCH p.parametros")
    List<PlantillaDE> findAllWithParametros();
}
