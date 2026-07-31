package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnalisisDetalleTO {
    private Long id;
    private String nroProtocolo;
    private String estado;
    private String tipoMuestraNombre;
    private String cliente;
    private Long clienteId;
    private Long userId;
    private String puntoMuestreo;
    private String fechaIngreso;
    private String fechaEntrega;
    private String observaciones;
    private String matrizNombre;
    private Long matrizId;
    private Long tipoMuestraId;

    private List<String> resolucionesAplicadas;
    private List<ParametroResultadoTO> parametros;
}