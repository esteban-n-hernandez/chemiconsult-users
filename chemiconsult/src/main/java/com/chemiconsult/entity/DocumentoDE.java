package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "DOCUMENTO")
public class DocumentoDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORIA_ID")
    private CategoriaDocumentoDE categoria;

    @Column(name = "FECHA_VENCIMIENTO")
    private LocalDate fechaVencimiento;

    @Column(name = "ARCHIVO_URL")
    private String archivoUrl;

    @Column(name = "NOMBRE_ARCHIVO")
    private String nombreArchivo;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;
}
