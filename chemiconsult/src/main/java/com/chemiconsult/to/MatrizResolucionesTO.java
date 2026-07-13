// MatrizResolucionesTO.java — árbol completo para el endpoint de matriz
package com.chemiconsult.to;

import com.chemiconsult.entity.ResolucionDE;
import lombok.Data;
import java.util.List;

@Data
public class MatrizResolucionesTO {
    private Long matrizId;
    private String matrizNombre;
    private List<ResolucionTO> resoluciones;
}