package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "CHEMI_MENSAJES")
public class MensajeDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMISOR_ID", nullable = false)
    private UserDE emisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECEPTOR_ID", nullable = false)
    private UserDE receptor;

    @Column(name = "CONTENIDO", length = 2000, nullable = false)
    private String contenido;

    @Column(name = "LEIDO", nullable = false)
    private boolean leido = false;

    @Column(name = "FECHA_ENVIO", nullable = false)
    private LocalDateTime fechaEnvio;

    @PrePersist
    protected void onCreate() {
        if (fechaEnvio == null) fechaEnvio = LocalDateTime.now();
    }
}
