package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "PLANTILLA")
@Data
public class PlantillaDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NOMBRE", nullable = false)
    private String nombre;

    @Column(name = "DESCRIPCION")
    private String descripcion;

    @Column(name = "ACTIVO")
    private Boolean activo = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "PLANTILLA_PARAMETRO",
            joinColumns = @JoinColumn(name = "PLANTILLA_ID"),
            inverseJoinColumns = @JoinColumn(name = "PARAMETRO_ID")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ParametroDE> parametros;

    @Column(name = "CREATED_DATE")
    private LocalDate createdDate;

    @Column(name = "UPDATE_DATE")
    private LocalDate updateDate;
}
