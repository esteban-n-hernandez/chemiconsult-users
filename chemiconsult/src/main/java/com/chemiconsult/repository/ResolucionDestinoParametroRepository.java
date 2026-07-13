package com.chemiconsult.repository;

import com.chemiconsult.entity.ResolucionDestinoParametroDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResolucionDestinoParametroRepository extends JpaRepository<ResolucionDestinoParametroDE, Long> {

    @Query("SELECT r FROM ResolucionDestinoParametroDE r " +
            "WHERE r.destino.id = :destinoId AND r.parametro.id = :parametroId")
    Optional<ResolucionDestinoParametroDE> findByDestinoIdAndParametroId(
            @Param("destinoId") Long destinoId,
            @Param("parametroId") Long parametroId);

    // Todos los límites definidos para un parámetro, dentro de una lista de destinos elegidos
    @Query("SELECT r FROM ResolucionDestinoParametroDE r " +
            "WHERE r.destino.id IN :destinoIds AND r.parametro.id = :parametroId")
    List<ResolucionDestinoParametroDE> findByDestinoIdsAndParametroId(
            @Param("destinoIds") List<Long> destinoIds,
            @Param("parametroId") Long parametroId);
}