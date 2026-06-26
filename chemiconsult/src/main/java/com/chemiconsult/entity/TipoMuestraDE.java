package com.chemiconsult.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "TIPO_MUESTRA")
@Data
public class TipoMuestraDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NOMBRE", nullable = false, unique = true)
    private String nombre; // "Agua potable", "Efluente industrial"

    @Column(name = "DESCRIPCION")
    private String descripcion;

    @Column(name = "ACTIVO")
    private Boolean activo = true;

    @Column(name = "CREATED_DATE")
    private LocalDate createdDate;

    @Column(name = "UPDATE_DATE")
    private LocalDate updateDate;

    // Parámetros por defecto para este tipo
    @ManyToMany
    @JoinTable(
            name = "TIPO_MUESTRA_PARAM",
            joinColumns = @JoinColumn(name = "TIPO_MUESTRA_ID"),
            inverseJoinColumns = @JoinColumn(name = "PARAMETRO_ID")
    )

    private List<ParametroDE> parametrosPorDefecto;
}