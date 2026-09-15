package com.chemiconsult.to;

import lombok.Data;

@Data
public class DeudorResumenTO {
    private Long   clienteId;
    private String clienteNombre;
    private String clienteCuit;
    private Double totalDeuda;
    private Long   cantidadFacturas;
}
