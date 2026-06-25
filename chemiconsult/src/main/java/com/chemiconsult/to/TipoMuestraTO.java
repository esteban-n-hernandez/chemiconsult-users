package com.chemiconsult.to;

import lombok.Data;

import java.util.List;

@Data
public class TipoMuestraTO {

    private String nombre;
    private String descripcion;

    // IDs de los parámetros por defecto que se asocian
    private List<Long> parametroIds;
}