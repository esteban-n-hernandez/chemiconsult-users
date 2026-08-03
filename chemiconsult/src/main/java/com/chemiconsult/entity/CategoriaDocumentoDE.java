package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "CATEGORIA_DOCUMENTO")
public class CategoriaDocumentoDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;
}
