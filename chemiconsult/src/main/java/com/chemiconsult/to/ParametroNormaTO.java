package com.chemiconsult.to;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class ParametroNormaTO {

    private Long id;
    private String nombre;
    private String unidad;
    private MetodologiaSimpleTO metodologia;

    private String tipoLimite;
    @JsonSerialize(using = ToStringSerializer.class)
    private Double valorMinimo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Double valorMaximo;
    private String limiteTexto;
}

