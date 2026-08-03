package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MuestreoTO {

    private Long id;
    private String tipo;
    private Long clienteId;
    private String clienteNombre;
    private Long sucursalId;
    private String sucursalNombre;
    private String fechaHora;
    private Long responsableId;
    private String responsableNombre;
    private String direccion;
    private String observaciones;
    private String documentacion;
    private String deQuien;
    private String estado;
}
