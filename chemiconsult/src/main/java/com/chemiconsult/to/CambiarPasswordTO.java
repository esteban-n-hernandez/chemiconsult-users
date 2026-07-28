package com.chemiconsult.to;

import lombok.Data;

@Data
public class CambiarPasswordTO {
    private String passwordActual;
    private String passwordNueva;
}