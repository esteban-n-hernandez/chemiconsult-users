package com.chemiconsult.mapper;

import com.chemiconsult.entity.MuestreoDE;
import com.chemiconsult.to.MuestreoTO;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MuestreoMapper {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static MuestreoTO toTO(MuestreoDE e) {
        String nombre = e.getCliente() != null
                ? (e.getCliente().getTipoCliente() != null && e.getCliente().getTipoCliente().name().equals("EMPRESA")
                    ? e.getCliente().getRazonSocial()
                    : (e.getCliente().getNombre() != null ? e.getCliente().getNombre() + " " + e.getCliente().getApellido() : ""))
                : "";

        return MuestreoTO.builder()
                .id(e.getId())
                .clienteId(e.getCliente() != null ? e.getCliente().getId() : null)
                .clienteNombre(nombre.trim())
                .sucursalId(e.getSucursal() != null ? e.getSucursal().getId() : null)
                .sucursalNombre(e.getSucursal() != null ? e.getSucursal().getNombre() : null)
                .fechaHora(e.getFechaHora() != null ? e.getFechaHora().format(FMT) : null)
                .responsableId(e.getResponsable() != null ? e.getResponsable().getId() : null)
                .responsableNombre(e.getResponsable() != null ? e.getResponsable().getUsername() : null)
                .direccion(e.getDireccion())
                .observaciones(e.getObservaciones())
                .estado(e.getEstado() != null ? e.getEstado().name() : null)
                .build();
    }

    public static List<MuestreoTO> toTO(List<MuestreoDE> list) {
        return list.stream().map(MuestreoMapper::toTO).toList();
    }
}
