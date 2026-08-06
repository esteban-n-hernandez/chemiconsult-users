package com.chemiconsult.to;

import lombok.Data;

@Data
public class ParametroNormaTO {

    private Long id;
    private String nombre;
    private String unidad;
    private MetodologiaSimpleTO metodologia;

    private String tipoLimite;
    private String valorMinimo;
    private String valorMaximo;
    private String limiteTexto;
}

