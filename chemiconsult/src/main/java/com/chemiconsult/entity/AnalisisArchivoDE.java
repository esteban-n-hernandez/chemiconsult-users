package com.chemiconsult.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "ANALISIS_ARCHIVOS")
@Data
public class AnalisisArchivoDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ANALISIS_ID", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AnalisisDE analisis;

    @Column(name = "ARCHIVO_URL", nullable = false)
    private String archivoUrl;

    @Column(name = "NOMBRE")
    private String nombre;

    @Column(name = "CREATED_AT")
    private LocalDate createdAt;

    @Column(name = "TIPO", length = 20)
    private String tipo;

    @Column(name = "DESACTUALIZADO")
    private Boolean desactualizado;
}
