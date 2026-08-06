package com.chemiconsult.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "ANALISIS_PARAM_LIMITE")
@Data
public class AnalisisParametroLimiteDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ANALISIS_PARAMETRO_ID", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AnalisisParametroDE analisisParametro;

    // De qué destino regulatorio sale este límite (Colectora cloacal, Pluvial, etc.)
    @ManyToOne
    @JoinColumn(name = "RESOL_DESTINO_PARAM_ID", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ResolucionDestinoParametroDE limiteOrigen;

    // Snapshot del límite al momento del análisis (auditoría, por si la norma cambia después)
    @Column(name = "LIMITE_MIN")
    private String limiteMin;

    @Column(name = "LIMITE_MAX")
    private String limiteMax;

    @Column(name = "LIMITE_TEXTO")
    private String limiteTexto;

    // Si el resultado cumple ESTE límite en particular (null hasta que haya resultado cargado)
    @Column(name = "CUMPLE")
    private Boolean cumple;
}