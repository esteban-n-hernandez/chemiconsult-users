package com.chemiconsult.to;

import lombok.Data;

@Data
public class ColaMuestraTO {
    private Long   analisisParametroId;
    private Long   analisisId;
    private String nroProtocolo;
    private String clienteNombre;
    private String puntoMuestreo;
    private String estado;
    private String fechaEntrega;
    private String estadoAnalisis;
}
