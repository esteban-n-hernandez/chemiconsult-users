package com.chemiconsult.repository;

import com.chemiconsult.entity.NumeradorDE;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NumeradorRepository extends JpaRepository<NumeradorDE, Long> {

    Optional<NumeradorDE> findByNombre(String nombre);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM NumeradorDE n WHERE n.nombre = :nombre")
    Optional<NumeradorDE> findByNombreForUpdate(@Param("nombre") String nombre);
}
