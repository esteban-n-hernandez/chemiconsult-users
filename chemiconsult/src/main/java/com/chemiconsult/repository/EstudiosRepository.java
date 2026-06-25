package com.chemiconsult.repository;

import com.chemiconsult.entity.AnalisisDE;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EstudiosRepository extends JpaRepository<AnalisisDE, Long> {
}