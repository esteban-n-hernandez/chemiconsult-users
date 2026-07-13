package com.chemiconsult.repository;

import com.chemiconsult.entity.ResolucionDestinoDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResolucionDestinoRepository extends JpaRepository<ResolucionDestinoDE, Long> {
    // JpaRepository ya nos da findById y findAll
}