package com.chemiconsult.repository;

import com.chemiconsult.entity.ParametroMetodologiaDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParametroMetodologiaRepository extends JpaRepository<ParametroMetodologiaDE, Long> {

    List<ParametroMetodologiaDE> findByParametroId(Long parametroId);

    boolean existsByParametroIdAndMetodologiaIdAndMatrizId(Long parametroId, Long metodologiaId, Long matrizId);

    boolean existsByParametroIdAndMetodologiaIdAndMatrizIsNull(Long parametroId, Long metodologiaId);

    boolean existsByIdAndParametroId(Long id, Long parametroId);
}
