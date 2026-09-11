package com.chemiconsult.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private String nombre;

    @Column(name = "UNIDAD")
    private String unidad;

    @Column(name = "ACTIVO")
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESPONSABLE_ID")
    @JsonIgnoreProperties({"password", "estudios", "modulos", "createdDate", "updateDate",
                            "hibernateLazyInitializer", "handler"})
    private UserDE responsable;

    @Column(name = "TIPO_ANALISIS")
    private String tipoAnalisis;

    @Column(name = "CREATED_DATE")
    private LocalDate createdDate;

    @Column(name = "UPDATE_DATE")
    private LocalDate updateDate;
}