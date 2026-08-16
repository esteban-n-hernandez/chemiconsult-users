package com.chemiconsult.to;

import lombok.Data;

@Data
public class FacturaItemTO {
    private String descripcion;
    private Double cantidad;
    private Double precioUnitario;
    private Double alicuotaIva;   // 0 / 10.5 / 21
}
