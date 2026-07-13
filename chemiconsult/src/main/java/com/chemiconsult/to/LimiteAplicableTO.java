package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LimiteAplicableTO {
    private String origenNombre; // "Res 336/06 - Colectora cloacal"
    private String tipoLimite;
    private Double limiteMin;
    private Double limiteMax;
    private String limiteTexto;
    private Boolean cumple;
}