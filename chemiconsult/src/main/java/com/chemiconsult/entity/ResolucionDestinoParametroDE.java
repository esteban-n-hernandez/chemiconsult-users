package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "RESOL_DESTINO_PARAM")
@Data
public class ResolucionDestinoParametroDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "RESOL_DESTINO_ID", nullable = false)
    private ResolucionDestinoDE destino;

    @ManyToOne
    @JoinColumn(name = "PARAMETRO_ID", nullable = false)
    private ParametroDE parametro;

    @ManyToOne
    @JoinColumn(name = "METODOLOGIA_ID")
    private MetodologiaDE metodologiaEstandar; // Metodología recomendada por la norma

    @Column(name = "TIPO_LIMITE")
    private String tipoLimite; // "MAX", "MIN", "RANGO", "TEXTO"

    @Column(name = "VALOR_MIN")
    private Double valorMinimo;

    @Column(name = "VALOR_MAX")
    private Double valorMaximo;

    @Column(name = "LIMITE_TEXTO")
    private String limiteTexto; // Ej: "Ausencia"
}