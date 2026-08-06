package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ParametroResultadoTO {
    private Long id;
    private String nombre;
    private String unidad;
    private Long metodologiaId;
    private String metodologiaNombre;
    private String valorResultado;
    private String observacion;
    private List<LimiteAplicableTO> limites;
}