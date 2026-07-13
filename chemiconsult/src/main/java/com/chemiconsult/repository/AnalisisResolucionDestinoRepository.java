package com.chemiconsult.repository;

import com.chemiconsult.entity.AnalisisResolucionDestinoDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalisisResolucionDestinoRepository extends JpaRepository<AnalisisResolucionDestinoDE, Long> {
}