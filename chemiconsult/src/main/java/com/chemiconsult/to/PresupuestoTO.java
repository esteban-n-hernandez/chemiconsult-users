package com.chemiconsult.to;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PresupuestoTO {

    private Long          numeroPresupuesto;
    private ClienteTO     cliente;
    private String        solicitadoPor;
    private LocalDate     fecha;
    private List<ItemPresupuestoTO> items;
}
