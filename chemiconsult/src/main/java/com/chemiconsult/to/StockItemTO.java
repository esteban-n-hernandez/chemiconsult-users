package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockItemTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String nivel;
    private String observaciones;

}
