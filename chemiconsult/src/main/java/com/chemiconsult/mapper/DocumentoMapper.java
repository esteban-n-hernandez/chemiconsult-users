package com.chemiconsult.mapper;

import com.chemiconsult.entity.DocumentoDE;
import com.chemiconsult.to.DocumentoTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DocumentoMapper {

    public static DocumentoTO toTO(DocumentoDE e) {
        return DocumentoTO.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .descripcion(e.getDescripcion())
                .categoria(e.getCategoria() != null ? e.getCategoria().name() : null)
                .fechaVencimiento(e.getFechaVencimiento() != null
                        ? e.getFechaVencimiento().format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                .estado(computarEstado(e.getFechaVencimiento()))
                .nombreArchivo(e.getNombreArchivo())
                .createdDate(e.getCreatedDate() != null
                        ? e.getCreatedDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                .build();
    }

    public static List<DocumentoTO> toTO(List<DocumentoDE> list) {
        return list.stream().map(DocumentoMapper::toTO).toList();
    }

    private static String computarEstado(LocalDate vencimiento) {
        if (vencimiento == null) return "VIGENTE";
        LocalDate hoy = LocalDate.now();
        if (vencimiento.isBefore(hoy)) return "VENCIDO";
        if (vencimiento.isBefore(hoy.plusDays(30))) return "PROXIMO_VENCER";
        return "VIGENTE";
    }
}
