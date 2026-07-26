package com.chemiconsult.to;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversacionTO {
    private Long otroUserId;
    private String otroUserNombre;
    private String ultimoMensaje;
    private LocalDateTime fechaUltimo;
    private int noLeidos;
}
