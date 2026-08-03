package com.chemiconsult.repository;

import com.chemiconsult.entity.CategoriaDocumentoDE;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaDocumentoRepository extends JpaRepository<CategoriaDocumentoDE, Long> {
    List<CategoriaDocumentoDE> findAllByOrderByNombreAsc();
    Optional<CategoriaDocumentoDE> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
