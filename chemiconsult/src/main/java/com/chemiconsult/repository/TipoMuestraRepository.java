package com.chemiconsult.repository;

import com.chemiconsult.entity.TipoMuestraDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoMuestraRepository extends JpaRepository<TipoMuestraDE, Long> {
    List<TipoMuestraDE> findByActivoTrue();
    List<TipoMuestraDE> findByMatrizIdAndActivoTrue(Long matrizId);

    boolean existsByNombreIgnoreCase(String nombre);

}
