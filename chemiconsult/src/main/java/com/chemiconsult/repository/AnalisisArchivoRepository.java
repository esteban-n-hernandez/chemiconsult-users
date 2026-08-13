package com.chemiconsult.repository;

import com.chemiconsult.entity.AnalisisArchivoDE;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalisisArchivoRepository extends JpaRepository<AnalisisArchivoDE, Long> {
    List<AnalisisArchivoDE> findAllByAnalisisIdOrderByCreatedAtAsc(Long analisisId);
    Optional<AnalisisArchivoDE> findByIdAndAnalisisId(Long id, Long analisisId);
    void deleteAllByAnalisisId(Long analisisId);
    boolean existsByAnalisisIdAndTipo(Long analisisId, String tipo);
    List<AnalisisArchivoDE> findAllByAnalisisIdInAndTipo(Iterable<Long> analisisIds, String tipo);
    Optional<AnalisisArchivoDE> findByAnalisisIdAndArchivoUrl(Long analisisId, String archivoUrl);
}
