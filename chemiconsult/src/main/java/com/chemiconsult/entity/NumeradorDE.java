package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "NUMERADORES")
@Data
public class NumeradorDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", unique = true, nullable = false)
    private String nombre;

    @Column(name = "valor", nullable = false)
    private Long valor;
}
