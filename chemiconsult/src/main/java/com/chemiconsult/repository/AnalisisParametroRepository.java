package com.chemiconsult.repository;

import com.chemiconsult.entity.AnalisisParametroDE;
import com.chemiconsult.enums.EstadoAnalisisParametroEnum;
import com.chemiconsult.enums.EstadoMuestraEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalisisParametroRepository extends JpaRepository<AnalisisParametroDE, Long> {

    @Query("""
        SELECT ap FROM AnalisisParametroDE ap
        JOIN FETCH ap.parametro p
        JOIN FETCH p.responsable r
        JOIN FETCH ap.analisis a
        JOIN FETCH a.cliente
        WHERE r.id = :userId
          AND ap.estadoAnalisis = :estado
          AND a.estado NOT IN :excluidos
        """)
    List<AnalisisParametroDE> findPendientesByResponsableId(
            @Param("userId")    Long userId,
            @Param("excluidos") List<EstadoMuestraEnum> excluidos,
            @Param("estado")    EstadoAnalisisParametroEnum estado);

    @Query("""
        SELECT ap FROM AnalisisParametroDE ap
        JOIN FETCH ap.parametro p
        JOIN FETCH p.responsable r
        JOIN FETCH ap.analisis a
        JOIN FETCH a.cliente
        WHERE r.id = :userId
          AND a.estado NOT IN :excluidos
        """)
    List<AnalisisParametroDE> findTodosByResponsableId(
            @Param("userId")    Long userId,
            @Param("excluidos") List<EstadoMuestraEnum> excluidos);

    @Query("""
        SELECT ap FROM AnalisisParametroDE ap
        JOIN FETCH ap.parametro p
        LEFT JOIN FETCH p.responsable
        JOIN FETCH ap.analisis a
        JOIN FETCH a.cliente
        WHERE a.estado NOT IN :excluidos
        """)
    List<AnalisisParametroDE> findAllExcluidos(
            @Param("excluidos") List<EstadoMuestraEnum> excluidos);
}
