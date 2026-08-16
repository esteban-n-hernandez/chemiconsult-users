package com.chemiconsult.wsmtxca;

import com.chemiconsult.enums.TipoComprobanteEnum;

import java.time.LocalDate;
import java.util.List;

/**
 * Datos necesarios para solicitar un CAE a ARCA (WSMTXCA FECAESolicitar).
 *
 * concepto : 1=Productos  2=Servicios  3=Productos+Servicios
 * docTipo  : 80=CUIT  86=CUIL  96=DNI  99=Consumidor Final
 */
public record CaeSolicitud(
        int               puntoVenta,
        TipoComprobanteEnum tipoComprobante,
        int               concepto,
        int               docTipo,
        String            docNro,
        long              cbteDesde,
        long              cbteHasta,
        LocalDate         cbteFch,
        double            impTotal,
        double            impNeto,
        double            impIva,
        List<AlicuotaIvaItem> alicuotasIva
) {}
