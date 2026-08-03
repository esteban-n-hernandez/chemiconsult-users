package com.chemiconsult.entity;

import com.chemiconsult.enums.CategoriaDocumentoEnum;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaDocumentoEnum categoria;

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
