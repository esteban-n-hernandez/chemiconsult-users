package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "METODOLOGIA")
@Data
public class MetodologiaDE {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NOMBRE", nullable = false, unique = true)
    private String nombre; // Ej: "Standard Methods 4500-H+ B", "EPA 200.8"

    @Column(name = "DESCRIPCION")
    private String descripcion; // Ej: "Método Potenciométrico", "ICP-MS"

    @Column(name = "ACTIVO")
    private Boolean activo = true;
}