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

    @ManyToOne
    @JoinColumn(name = "METODOLOGIA_ID")
    private MetodologiaDE metodologiaUsada; // <-- NUEVO: Guarda el método real del ensayo

    @Column(name = "VALOR_RESULTADO")
    private String valorResultado;

    @Column(name = "CUMPLE_NORMA")
    private Boolean cumpleNorma;

    // <-- NUEVOS: Copia de los límites aplicados en este análisis para auditoría futura
    @Column(name = "LIMITE_APLICADO_MIN")
    private Double limiteAplicadoMin;

    @Column(name = "LIMITE_APLICADO_MAX")
    private Double limiteAplicadoMax;

    @Column(name = "LIMITE_APLICADO_TEXTO")
    private String limiteAplicadoTexto;

    @Column(name = "OBSERVACION")
    private String observacion;
}