package com.chemiconsult.wsmtxca;

import com.chemiconsult.enums.TipoComprobanteEnum;

/**
 * Abstracción del Web Service de Facturación Electrónica de ARCA (WSMTXCA).
 * La implementación activa es {@link WsmtxcaMockPort}; cuando se integre el
 * cliente SOAP real se crea una segunda implementación sin tocar este contrato.
 */
public interface WsmtxcaPort {

    /** Devuelve el último número de comprobante autorizado para el punto de venta y tipo dados. */
    long getUltimoComprobanteAutorizado(int puntoVenta, TipoComprobanteEnum tipo);

    /** Solicita la autorización electrónica (CAE) para un comprobante. */
    CaeResponse solicitarCAE(CaeSolicitud solicitud);
}
