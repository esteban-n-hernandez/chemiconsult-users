package com.chemiconsult.mapper;

import com.chemiconsult.entity.StockItemDE;
import com.chemiconsult.enums.CategoriaStockEnum;
import com.chemiconsult.enums.NivelStockEnum;
import com.chemiconsult.to.StockItemTO;

import java.util.List;

public class StockItemMapper {

    public static List<StockItemTO> mapToTO(List<StockItemDE> items) {
        return items.stream().map(StockItemMapper::mapToTO).toList();
    }

    public static StockItemTO mapToTO(StockItemDE item) {
        return StockItemTO.builder()
                .id(item.getId())
                .nombre(item.getNombre())
                .descripcion(item.getDescripcion())
                .categoria(item.getCategoria() != null ? item.getCategoria().name() : null)
                .nivel(item.getNivel() != null ? item.getNivel().name() : null)
                .observaciones(item.getObservaciones())
                .cantidadFrascos(item.getCantidadFrascos())
                .ubicacion(item.getUbicacion())
                .build();
    }

    public static StockItemDE mapToEntity(StockItemTO to) {
        StockItemDE entity = new StockItemDE();
        entity.setNombre(to.getNombre());
        entity.setDescripcion(to.getDescripcion());
        entity.setCategoria(CategoriaStockEnum.valueOf(to.getCategoria()));
        entity.setNivel(NivelStockEnum.valueOf(to.getNivel()));
        entity.setObservaciones(to.getObservaciones());
        entity.setCantidadFrascos(to.getCantidadFrascos());
        entity.setUbicacion(to.getUbicacion());
        return entity;
    }

    public static StockItemDE applyUpdate(StockItemDE entity, StockItemTO to) {
        entity.setNombre(to.getNombre());
        entity.setDescripcion(to.getDescripcion());
        entity.setCategoria(CategoriaStockEnum.valueOf(to.getCategoria()));
        entity.setNivel(NivelStockEnum.valueOf(to.getNivel()));
        entity.setObservaciones(to.getObservaciones());
        entity.setCantidadFrascos(to.getCantidadFrascos());
        entity.setUbicacion(to.getUbicacion());
        return entity;
    }
}
