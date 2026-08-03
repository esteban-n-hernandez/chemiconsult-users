package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentoTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String fechaVencimiento;
    private String estado;
    private String nombreArchivo;
    private String createdDate;
}
