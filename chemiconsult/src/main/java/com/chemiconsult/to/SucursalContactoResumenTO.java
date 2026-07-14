package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SucursalContactoResumenTO {
    private Long contactoId;
    private String nombre;
    private String email;
    private String telefono;
}