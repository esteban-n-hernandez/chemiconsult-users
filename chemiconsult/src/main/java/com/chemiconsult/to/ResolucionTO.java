// ResolucionTO.java
package com.chemiconsult.to;

import lombok.Data;
import java.util.List;

@Data
public class ResolucionTO {
    private Long id;
    private String nombre;
    private Boolean tieneDestino;
    private List<ResolucionDestinoTO> destinos;
}