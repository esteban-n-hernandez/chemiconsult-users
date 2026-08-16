package com.chemiconsult.repository;

import com.chemiconsult.entity.FacturaDE;
import com.chemiconsult.enums.TipoComprobanteEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacturaRepository extends JpaRepository<FacturaDE, Long> {

    List<FacturaDE> findAllByOrderByNumeroDesc();

    @Query("SELECT COALESCE(MAX(f.numero), 0) FROM FacturaDE f WHERE f.puntoVenta = :pv AND f.tipoComprobante = :tipo")
    long findUltimoNumero(@Param("pv") int puntoVenta, @Param("tipo") TipoComprobanteEnum tipo);
}
