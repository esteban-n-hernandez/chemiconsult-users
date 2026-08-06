package com.chemiconsult.to;

import lombok.Data;
import java.util.List;

@Data
public class ColaAnalisisTO {
    private Long   parametroId;
    private String parametroNombre;
    private String unidad;
    private Long   responsableId;
    private String responsableNombre;
    private int    totalPendientes;
    private int    totalAnalizado;
    private int    totalConfirmado;
    private int    totalObservado;
    private List<ColaMuestraTO> muestras;
}
