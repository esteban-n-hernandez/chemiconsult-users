package com.chemiconsult.to;

import lombok.Data;

@Data
public class ParametroNormaTO {

    private Long id;
    private String nombre;
    private String unidad;
    private MetodologiaSimpleTO metodologia;

    // Límites opcionales por si querés mostrarlos en el frontend en el futuro
    private String tipoLimite;
    private Double valorMinimo;
    private Double valorMaximo;
    private String limiteTexto;
}

