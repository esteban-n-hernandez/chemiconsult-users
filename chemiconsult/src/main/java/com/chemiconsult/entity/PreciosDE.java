package com.chemiconsult.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "PRECIOS")
@Data
public class PreciosDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NOMBRE_SERVICIO")
    private String nombreServicio;

    @Column(name = "PRECIO")
    private Double precio;

    @Column(name = "PRECIO_CON_IMPUESTO")
    private Double precioConImpuesto;

    @Column(name = "DESCRIPCION")
    private String descripcion;
}
