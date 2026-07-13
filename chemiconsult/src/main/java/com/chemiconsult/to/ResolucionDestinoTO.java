package com.chemiconsult.to;

import lombok.Data;
import java.util.List;

@Data
public class ResolucionDestinoTO {
    private Long id;
    private String nombre;
    private List<ParametroNormaTO> parametros;
}