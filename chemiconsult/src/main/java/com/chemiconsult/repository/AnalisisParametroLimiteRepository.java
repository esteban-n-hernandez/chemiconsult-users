package com.chemiconsult.repository;

import com.chemiconsult.entity.AnalisisParametroLimiteDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalisisParametroLimiteRepository extends JpaRepository<AnalisisParametroLimiteDE, Long> {
}