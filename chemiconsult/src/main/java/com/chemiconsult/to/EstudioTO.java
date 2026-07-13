package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EstudioTO {

    private Long id;
    private String cliente;
    private String tipo;
    private String estado;
    private String archivoUrl;
    private Long userId;
    private String userMail;
    private String createdDate;

    private String nroProtocolo;
    private String fechaIngreso;
    private String fechaEntrega;
    private String idMuestra;
    private String puntoMuestreo;
    private Long matrizId;
    private List<Long> resolucionDestinoIds;
    private String observaciones;
    private List<Long> parametrosIds;
}