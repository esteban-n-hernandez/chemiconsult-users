package com.chemiconsult.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "RESOL_DESTINO_PARAM")
@Data
public class ResolucionDestinoParametroDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "RESOL_DESTINO_ID", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ResolucionDestinoDE destino;

    @ManyToOne
    @JoinColumn(name = "PARAMETRO_ID", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ParametroDE parametro;

    @ManyToOne
    @JoinColumn(name = "METODOLOGIA_ID")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MetodologiaDE metodologiaEstandar; // Metodología recomendada por la norma

    @Column(name = "UNIDAD")
    private String unidad;

    @Column(name = "TIPO_LIMITE", nullable = false)
    private String tipoLimite; // "MAX", "MIN", "RANGO", "TEXTO", "AUSENCIA", "NE" (no exigido)

    @Column(name = "VALOR_MIN")
    @JsonSerialize(using = ToStringSerializer.class)
    private Double valorMinimo;

    @Column(name = "VALOR_MAX")
    @JsonSerialize(using = ToStringSerializer.class)
    private Double valorMaximo;

    @Column(name = "LIMITE_TEXTO")
    private String limiteTexto; // "Ausencia", "Sin olores extraños", etc.

    @Column(name = "ACTIVO")
    private Boolean activo = true;
}