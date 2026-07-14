package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClienteSucursalTO {
    private Long id;
    private Long clienteId;
    private String nombre;
    private String direccion;
    private String localidad;
    private String provincia;
    private Boolean activo;
}