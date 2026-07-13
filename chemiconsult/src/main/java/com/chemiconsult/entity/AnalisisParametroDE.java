package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

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
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ParametroDE parametro;

    @ManyToOne
    @JoinColumn(name = "METODOLOGIA_ID")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MetodologiaDE metodologiaUsada;

    @Column(name = "VALOR_RESULTADO")
    private String valorResultado;

    // Límites aplicables a este parámetro (uno por cada destino elegido en la muestra)
    @OneToMany(mappedBy = "analisisParametro", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<AnalisisParametroLimiteDE> limites;

    @Column(name = "OBSERVACION")
    private String observacion;
}