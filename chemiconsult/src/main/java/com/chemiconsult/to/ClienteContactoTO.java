package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ClienteContactoTO {
    private Long id;
    private Long clienteId;
    private String nombre;
    private String email;
    private String telefono;
    private Boolean activo;
    private List<Long> sucursalIds; // en qué sucursales participa este contacto
}