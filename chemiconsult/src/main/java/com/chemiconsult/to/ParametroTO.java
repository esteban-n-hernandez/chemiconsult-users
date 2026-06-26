package com.chemiconsult.to;

import lombok.Data;

@Data
public class ParametroTO {

    private String nombre;
    private String unidad;
    private Double valorMinimo;
    private Double valorMaximo;
    private String normaReferencia;
    private Boolean activo;
}
