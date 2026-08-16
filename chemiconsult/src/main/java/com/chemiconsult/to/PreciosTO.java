package com.chemiconsult.to;

import lombok.Data;

@Data
public class PreciosTO {

    private Long id;
    private String nombreServicio;
    private Double precio;
    private Double precioConImpuesto;
    private String descripcion;

}
