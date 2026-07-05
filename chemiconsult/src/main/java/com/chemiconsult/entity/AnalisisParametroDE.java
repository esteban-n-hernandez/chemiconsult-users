package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "ANALISIS_PARAMETRO")
@Data
public class AnalisisParametroDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ANALISIS_ID", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AnalisisDE analisis;

    @ManyToOne
    @JoinColumn(name = "PARAMETRO_ID", nullable = false)
    private ParametroDE parametro;

    @Column(name = "VALOR_RESULTADO")
    private String valorResultado; // String para cubrir "<0.1", "Ausencia", etc.

    @Column(name = "CUMPLE_NORMA")
    private Boolean cumpleNorma;

    @Column(name = "OBSERVACION")
    private String observacion;
}
