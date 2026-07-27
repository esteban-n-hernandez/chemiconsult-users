package com.chemiconsult.to;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MensajeGrupoTO {
    private Long id;
    private Long emisorId;
    private String emisorNombre;
    private String contenido;
    private LocalDateTime fechaEnvio;
}
