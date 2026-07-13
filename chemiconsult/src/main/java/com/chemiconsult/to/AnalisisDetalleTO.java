package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnalisisDetalleTO {
    private Long id;
    private String nroProtocolo;
    private String idMuestra;
    private String estado;
    private String cliente;
    private Long userId;
    private String puntoMuestreo;
    private String fechaIngreso;
    private String fechaEntrega;
    private String observaciones;
    private String archivoUrl;
    private String matrizNombre;

    private List<String> resolucionesAplicadas;
    private List<ParametroResultadoTO> parametros;
}