package com.chemiconsult.wsmtxca;

/**
 * Desglose de IVA por alícuota para la solicitud de CAE.
 * Códigos ARCA: 3=0%, 4=10.5%, 5=21%, 6=27%, 8=5%, 9=2.5%
 */
public record AlicuotaIvaItem(int id, double baseImp, double importe) {}
