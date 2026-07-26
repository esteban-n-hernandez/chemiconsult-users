package com.chemiconsult.to;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MensajeTO {
    private Long id;
    private Long emisorId;
    private String emisorNombre;
    private Long receptorId;
    private String contenido;
    private boolean leido;
    private LocalDateTime fechaEnvio;
}
