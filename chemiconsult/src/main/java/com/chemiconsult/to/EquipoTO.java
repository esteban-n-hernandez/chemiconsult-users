package com.chemiconsult.to;

import lombok.Data;

@Data
public class EquipoTO {
    private Long id;
    private String nombre;
    private String marca;
    private String modelo;
    private String nroSerie;
    private String certificacion;
    private String vencimiento;
    private Boolean activo;
}
