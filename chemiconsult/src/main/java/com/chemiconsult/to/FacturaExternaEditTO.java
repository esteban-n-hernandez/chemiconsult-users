package com.chemiconsult.to;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FacturaExternaEditTO {
    private LocalDate fechaEmision;
    private String    tipoComprobante;
    private Integer   puntoVenta;
    private Long      numero;
    private String    clienteNombre;
    private String    clienteCuit;
    private String    clienteCondicionIVA;
    private Double    total;
    private String    descripcion;
}
