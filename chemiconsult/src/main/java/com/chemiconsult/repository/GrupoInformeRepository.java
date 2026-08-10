package com.chemiconsult.repository;

import com.chemiconsult.entity.GrupoInformeDE;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrupoInformeRepository extends JpaRepository<GrupoInformeDE, Long> {
    List<GrupoInformeDE> findAllByOrderByOrdenAsc();
}
