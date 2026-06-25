package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "PARAMETRO")
@Data
public class ParametroDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NOMBRE", nullable = false)
    private String nombre; // "pH", "Turbidez", "Coliformes totales"

    @Column(name = "UNIDAD")
    private String unidad; // "mg/L", "NTU", "UFC/100mL"

    @Column(name = "VALOR_MIN")
    private Double valorMinimo; // límite normativo

    @Column(name = "VALOR_MAX")
    private Double valorMaximo;

    @Column(name = "NORMA_REFERENCIA")
    private String normaReferencia; // "CAA Art. 982", "Ley 24051"

    @Column(name = "ACTIVO")
    private Boolean activo = true;

    @Column(name = "CREATED_DATE")
    private LocalDate createdDate;

    // ── Agregar esto ──
    @Column(name = "UPDATE_DATE")
    private LocalDate updateDate;
}
