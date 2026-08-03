package com.chemiconsult.entity;

import com.chemiconsult.enums.CategoriaStockEnum;
import com.chemiconsult.enums.NivelStockEnum;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "STOCK_ITEM")
public class StockItemDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaStockEnum categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelStockEnum nivel;

    @Column(length = 1000)
    private String observaciones;

    @Column(name = "CANTIDAD_FRASCOS")
    private Integer cantidadFrascos;

    @Column(name = "UBICACION", length = 200)
    private String ubicacion;

}
