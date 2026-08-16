package com.chemiconsult.mapper;

import com.chemiconsult.entity.PreciosDE;
import com.chemiconsult.to.PreciosTO;

public class PricingMapper {

    public static PreciosDE toEntity(PreciosTO preciosTO) {
        PreciosDE preciosDE = new PreciosDE();
        preciosDE.setNombreServicio(preciosTO.getNombreServicio());
        preciosDE.setPrecio(preciosTO.getPrecio());
        preciosDE.setPrecioConImpuesto(preciosTO.getPrecioConImpuesto());
        preciosDE.setDescripcion(preciosTO.getDescripcion());
        return preciosDE;
    }

    public static PreciosTO toTO(PreciosDE preciosDE) {
        PreciosTO preciosTO = new PreciosTO();
        preciosTO.setId(preciosDE.getId());
        preciosTO.setNombreServicio(preciosDE.getNombreServicio());
        preciosTO.setPrecio(preciosDE.getPrecio());
        preciosTO.setPrecioConImpuesto(preciosDE.getPrecioConImpuesto());
        preciosTO.setDescripcion(preciosDE.getDescripcion());
        return preciosTO;
    }
}
