package com.chemiconsult.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "PARAMETRO_METODOLOGIA")
@Data
public class ParametroMetodologiaDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARAMETRO_ID", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ParametroDE parametro;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "METODOLOGIA_ID", nullable = false)
    private MetodologiaDE metodologia;

    // null = metodología válida para cualquier matriz
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MATRIZ_ID")
    private MatrizDE matriz;

    @Column(name = "TIPO_ANALISIS")
    private String tipoAnalisis;
}
