package com.chemiconsult.mapper;

import com.chemiconsult.entity.ParametroDE;
import com.chemiconsult.to.ParametroTO;

import java.time.LocalDate;

public class ParametroMapper {

    public static ParametroDE createParametro(ParametroTO to) {
        ParametroDE parametro = new ParametroDE();
        parametro.setNombre(to.getNombre());
        parametro.setUnidad(to.getUnidad());
        parametro.setValorMinimo(to.getValorMinimo());
        parametro.setValorMaximo(to.getValorMaximo());
        parametro.setNormaReferencia(to.getNormaReferencia());
        parametro.setActivo(true);
        parametro.setCreatedDate(LocalDate.now());
        parametro.setUpdateDate(LocalDate.now());
        return parametro;
    }

    public static ParametroDE updateParametro(ParametroDE existing, ParametroTO to) {
        existing.setNombre(to.getNombre());
        existing.setUnidad(to.getUnidad());
        existing.setValorMinimo(to.getValorMinimo());
        existing.setValorMaximo(to.getValorMaximo());
        existing.setNormaReferencia(to.getNormaReferencia());
        existing.setUpdateDate(LocalDate.now());
        return existing;
    }
}
