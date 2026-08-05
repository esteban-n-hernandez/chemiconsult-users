package com.chemiconsult.to;

import lombok.Data;
import java.util.List;

@Data
public class ColaAnalisisTO {
    private Long            parametroId;
    private String          parametroNombre;
    private String          unidad;
    private int             totalPendientes;
    private int             totalAnalizado;
    private List<ColaMuestraTO> muestras;
}
