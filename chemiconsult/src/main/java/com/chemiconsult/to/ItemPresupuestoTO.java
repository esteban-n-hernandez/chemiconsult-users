package com.chemiconsult.to;

import lombok.Data;

@Data
public class ItemPresupuestoTO {
    private String matriz;
    private String determinacion;
    private Double precioUnitario;
    private Long   cantidadMuestras;
    private Double total;
}
