package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "GRUPO_INFORME")
@Data
public class GrupoInformeDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CODIGO", nullable = false, unique = true)
    private String codigo;

    @Column(name = "LABEL", nullable = false)
    private String label;

    @Column(name = "ORDEN")
    private Integer orden = 0;
}
