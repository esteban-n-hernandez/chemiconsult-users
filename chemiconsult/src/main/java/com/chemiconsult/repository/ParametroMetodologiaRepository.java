package com.chemiconsult.repository;

import com.chemiconsult.entity.ParametroMetodologiaDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParametroMetodologiaRepository extends JpaRepository<ParametroMetodologiaDE, Long> {

    List<ParametroMetodologiaDE> findByParametroId(Long parametroId);

    @Query("SELECT pm FROM ParametroMetodologiaDE pm WHERE pm.parametro.id = :parametroId AND (pm.matriz IS NULL OR pm.matriz.id = :matrizId)")
    List<ParametroMetodologiaDE> findByParametroIdAndMatriz(@Param("parametroId") Long parametroId, @Param("matrizId") Long matrizId);

    boolean existsByParametroIdAndMetodologiaIdAndMatrizId(Long parametroId, Long metodologiaId, Long matrizId);

    boolean existsByParametroIdAndMetodologiaIdAndMatrizIsNull(Long parametroId, Long metodologiaId);

    boolean existsByIdAndParametroId(Long id, Long parametroId);

    java.util.Optional<ParametroMetodologiaDE> findByParametroIdAndMetodologiaId(Long parametroId, Long metodologiaId);
}
