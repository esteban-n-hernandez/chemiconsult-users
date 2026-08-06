package com.chemiconsult.entity;

import com.chemiconsult.enums.PresupuestoEstadoEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PRESUPUESTO")
@Data
public class PresupuestoDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", unique = true, nullable = false)
    private Long numero;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "solicitado_por")
    private String solicitadoPor;

    @Column(name = "cliente_nombre")
    private String clienteNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private PresupuestoEstadoEnum estado;

    @Column(name = "fecha_respuesta")
    private LocalDate fechaRespuesta;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "presupuesto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<PresupuestoItemDE> items = new ArrayList<>();

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (estado == null) estado = PresupuestoEstadoEnum.PENDIENTE;
    }
}
