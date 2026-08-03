package com.chemiconsult.enums;

public enum TipoEventoAgendaEnum {
    MUESTREO,
    COMPRA_INSUMOS,
    VENCIMIENTO,
    OTRO,
    /** @deprecated use VENCIMIENTO */
    @Deprecated DOCUMENTACION,
    /** @deprecated use VENCIMIENTO */
    @Deprecated VISITA_TECNICA,
    /** @deprecated removed */
    @Deprecated ANALISIS
}
