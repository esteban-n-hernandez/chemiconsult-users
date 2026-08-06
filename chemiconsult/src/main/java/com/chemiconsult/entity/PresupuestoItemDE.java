package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "PRESUPUESTO_ITEM")
@Data
public class PresupuestoItemDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "PRESUPUESTO_ID", nullable = false)
    private PresupuestoDE presupuesto;

    @Column(name = "orden")
    private Integer orden;

    @Column(name = "matriz")
    private String matriz;

    @Column(name = "determinacion", length = 2000)
    private String determinacion;

    @Column(name = "precio_unitario")
    private Double precioUnitario;

    @Column(name = "cantidad_muestras")
    private Long cantidadMuestras;

    @Column(name = "total")
    private Double total;
}
