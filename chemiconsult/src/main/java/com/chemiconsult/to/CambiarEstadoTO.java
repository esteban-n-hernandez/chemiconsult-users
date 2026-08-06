package com.chemiconsult.to;

import com.chemiconsult.enums.PresupuestoEstadoEnum;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CambiarEstadoTO {
    private PresupuestoEstadoEnum estado;
    private LocalDate fechaRespuesta;
    private String observaciones;
}
