package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "FACTURA_ITEM")
@Data
public class FacturaItemDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "FACTURA_ID", nullable = false)
    private FacturaDE factura;

    @Column(name = "orden")
    private Integer orden;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "cantidad")
    private Double cantidad;

    @Column(name = "precio_unitario")
    private Double precioUnitario;

    @Column(name = "alicuota_iva")
    private Double alicuotaIva;

    @Column(name = "subtotal")
    private Double subtotal;

    @Column(name = "importe_iva")
    private Double importeIva;

    @Column(name = "total")
    private Double total;
}
