package com.chemiconsult.to;

import com.chemiconsult.enums.PresupuestoEstadoEnum;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PresupuestoResumenTO {
    private Long id;
    private Long numero;
    private LocalDate fecha;
    private String clienteNombre;
    private String solicitadoPor;
    private Double totalGeneral;
    private PresupuestoEstadoEnum estado;
    private LocalDate fechaRespuesta;
    private String observaciones;
    private LocalDateTime createdAt;
}
