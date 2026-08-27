package com.chemiconsult.to;

import lombok.Data;

import java.util.List;

@Data
public class PlantillaTO {
    private String nombre;
    private String descripcion;
    private List<Long> parametroIds;
}
