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

    @Query("""
        SELECT a FROM AnalisisDE a
        LEFT JOIN FETCH a.cliente
        LEFT JOIN FETCH a.user
        LEFT JOIN FETCH a.tipoMuestra
        LEFT JOIN FETCH a.matriz
        LEFT JOIN FETCH a.sucursal
        ORDER BY a.id DESC
        """)
    List<AnalisisDE> findAllWithAssociations();

    @Query("""
        SELECT a FROM AnalisisDE a
        LEFT JOIN FETCH a.cliente
        LEFT JOIN FETCH a.user
        LEFT JOIN FETCH a.tipoMuestra
        LEFT JOIN FETCH a.matriz
        LEFT JOIN FETCH a.sucursal
        WHERE a.estado IN :estados
        ORDER BY a.id DESC
        """)
    List<AnalisisDE> findAllByEstadoInWithAssociations(@Param("estados") List<EstadoMuestraEnum> estados);

    @Query("""
        SELECT a FROM AnalisisDE a
        LEFT JOIN FETCH a.cliente
        LEFT JOIN FETCH a.user
        LEFT JOIN FETCH a.tipoMuestra
        LEFT JOIN FETCH a.matriz
        LEFT JOIN FETCH a.sucursal
        WHERE a.cliente = :cliente
        ORDER BY a.id DESC
        """)
    List<AnalisisDE> findAllByClienteWithAssociations(@Param("cliente") ClienteDE cliente);

    @Query("SELECT a.estado, COUNT(a) FROM AnalisisDE a WHERE a.fechaIngreso BETWEEN :desde AND :hasta GROUP BY a.estado")
    List<Object[]> countByEstadoInMonth(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}