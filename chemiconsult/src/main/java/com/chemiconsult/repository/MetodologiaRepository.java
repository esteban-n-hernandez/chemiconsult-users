package com.chemiconsult.repository;

import com.chemiconsult.entity.MetodologiaDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetodologiaRepository extends JpaRepository<MetodologiaDE, Long> {
    List<MetodologiaDE> findByActivoTrue();
}