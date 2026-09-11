package com.chemiconsult.repository;

import com.chemiconsult.entity.AnalisisDE;
import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.entity.UserDE;
import com.chemiconsult.enums.EstadoMuestraEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AnalisisRepository extends JpaRepository<AnalisisDE, Long> {
    List<AnalisisDE> findAllByUser(UserDE user);
    List<AnalisisDE> findAllByCliente(ClienteDE cliente);
    boolean existsByNumeroProtocolo(String numeroProtocolo);
    List<AnalisisDE> findAllByFechaIngresoBetweenOrderByFechaIngresoAsc(LocalDate desde, LocalDate hasta);
    List<AnalisisDE> findAllByEstadoIn(List<EstadoMuestraEnum> estados);

    @Query("SELECT a.estado, COUNT(a) FROM AnalisisDE a WHERE a.fechaIngreso BETWEEN :desde AND :hasta GROUP BY a.estado")
    List<Object[]> countByEstadoInMonth(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}