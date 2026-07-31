package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalisisArchivoTO {
    private Long id;
    private String nombre;
    private String createdAt;
}
