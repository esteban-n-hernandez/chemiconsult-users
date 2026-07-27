package com.chemiconsult.repository;

import com.chemiconsult.entity.MensajeGrupoDE;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensajeGrupoRepository extends JpaRepository<MensajeGrupoDE, Long> {

    @Query("SELECT m FROM MensajeGrupoDE m ORDER BY m.id DESC")
    List<MensajeGrupoDE> findUltimos(Pageable pageable);

    @Query("SELECT m FROM MensajeGrupoDE m WHERE m.id < :antesDeId ORDER BY m.id DESC")
    List<MensajeGrupoDE> findAntesDe(@Param("antesDeId") Long antesDeId, Pageable pageable);

    @Query("SELECT m FROM MensajeGrupoDE m WHERE m.id > :despuesDeId ORDER BY m.id ASC")
    List<MensajeGrupoDE> findDespuesDe(@Param("despuesDeId") Long despuesDeId);
}
