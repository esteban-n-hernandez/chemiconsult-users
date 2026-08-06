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

    @Query("SELECT ap FROM AnalisisParametroDE ap " +
           "WHERE ap.parametro.responsable.id = :userId " +
           "AND ap.estadoAnalisis = :estado " +
           "AND ap.analisis.estado NOT IN :excluidos")
    List<AnalisisParametroDE> findPendientesByResponsableId(
            @Param("userId")    Long userId,
            @Param("excluidos") List<EstadoMuestraEnum> excluidos,
            @Param("estado")    EstadoAnalisisParametroEnum estado);

    @Query("SELECT ap FROM AnalisisParametroDE ap " +
           "WHERE ap.parametro.responsable.id = :userId " +
           "AND ap.analisis.estado NOT IN :excluidos")
    List<AnalisisParametroDE> findTodosByResponsableId(
            @Param("userId")    Long userId,
            @Param("excluidos") List<EstadoMuestraEnum> excluidos);

    @Query("SELECT ap FROM AnalisisParametroDE ap " +
           "WHERE ap.analisis.estado NOT IN :excluidos")
    List<AnalisisParametroDE> findAllExcluidos(
            @Param("excluidos") List<EstadoMuestraEnum> excluidos);
}
