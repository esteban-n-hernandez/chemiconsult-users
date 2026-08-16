package com.chemiconsult.enums;

public enum TipoComprobanteEnum {
    A(1,  "Factura A"),
    B(6,  "Factura B"),
    C(11, "Factura C");

    public final int codigo;
    public final String descripcion;

    TipoComprobanteEnum(int codigo, String descripcion) {
        this.codigo      = codigo;
        this.descripcion = descripcion;
    }
}
