package com.chemiconsult.repository;

import com.chemiconsult.entity.EstudiosDE;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EstudiosRepository extends JpaRepository<EstudiosDE, Long> {
}