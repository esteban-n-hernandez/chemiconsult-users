package com.chemiconsult.repository;

import com.chemiconsult.entity.MatrizDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatrizRepository extends JpaRepository<MatrizDE, Long> {
    Optional<MatrizDE> findByNombreIgnoreCase(String nombre);
}