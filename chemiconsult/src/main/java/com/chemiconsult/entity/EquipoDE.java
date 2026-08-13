package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "EQUIPO")
@Data
public class EquipoDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String marca;

    @Column
    private String modelo;

    @Column(name = "NRO_SERIE")
    private String nroSerie;

    @Column(length = 500)
    private String certificacion;

    @Column(name = "VENCIMIENTO")
    private LocalDate vencimiento;

    @Column(nullable = false)
    private Boolean activo = true;
}
