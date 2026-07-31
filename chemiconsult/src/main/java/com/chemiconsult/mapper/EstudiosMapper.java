package com.chemiconsult.mapper;

import com.chemiconsult.entity.*;
import com.chemiconsult.enums.EstadoMuestraEnum;
import com.chemiconsult.to.*;

import java.time.LocalDate;
import java.util.List;

public class EstudiosMapper {

    public static AnalisisDE createEstudio(EstudioTO estudio, ClienteDE cliente, MatrizDE matriz, ClienteSucursalDE sucursal) {
        AnalisisDE entity = new AnalisisDE();
        entity.setCliente(cliente);
        entity.setUser(cliente.getUser());
        entity.setMatriz(matriz);
        entity.setSucursal(sucursal);
        entity.setNumeroProtocolo(estudio.getNroProtocolo());
        entity.setPuntoMuestreo(estudio.getPuntoMuestreo());
        entity.setObservaciones(estudio.getObservaciones());
        entity.setEstado(EstadoMuestraEnum.PENDIENTE);

        entity.setFechaIngreso(estudio.getFechaIngreso() != null
                ? LocalDate.parse(estudio.getFechaIngreso()) : LocalDate.now());
        entity.setFechaEntrega(estudio.getFechaEntrega() != null
                ? LocalDate.parse(estudio.getFechaEntrega()) : null);

        entity.setCreatedDate(LocalDate.now());
        entity.setUpdateDate(LocalDate.now());

        return entity;
    }

    public static EstudioTO mapEntityToEstudioTO(AnalisisDE entity) {
        return EstudioTO.builder()
                .id(entity.getId())
                .cliente(resolverNombreCliente(entity))
                .estado(entity.getEstado() != null ? entity.getEstado().name() : null)
                .tipo(entity.getTipo())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userMail(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .nroProtocolo(entity.getNumeroProtocolo())
                .fechaIngreso(entity.getFechaIngreso() != null ? entity.getFechaIngreso().toString() : null)
                .fechaEntrega(entity.getFechaEntrega() != null ? entity.getFechaEntrega().toString() : null)
                .createdDate(entity.getCreatedDate() != null ? entity.getCreatedDate().toString() : null)
                .build();
    }

    public static AnalisisDetalleTO mapEntityToDetalleTO(AnalisisDE entity) {
        String clienteStr = resolverNombreCliente(entity);

        List<String> resoluciones = entity.getResolucionesAplicadas() == null
                ? List.of()
                : entity.getResolucionesAplicadas().stream()
                .map(ard -> ard.getResolucionDestino().getResolucion().getNombre()
                            + " - " + ard.getResolucionDestino().getNombre())
                .toList();

        List<ParametroResultadoTO> parametros = entity.getParametros() == null
                ? List.of()
                : entity.getParametros().stream()
                .map(EstudiosMapper::mapParametroToResultadoTO)
                .toList();

        return AnalisisDetalleTO.builder()
                .id(entity.getId())
                .nroProtocolo(entity.getNumeroProtocolo())
                .tipoMuestraNombre(entity.getTipoMuestra() != null ? entity.getTipoMuestra().getNombre() : null)
                .estado(entity.getEstado() != null ? entity.getEstado().name() : null)
                .cliente(clienteStr)
                .clienteId(entity.getCliente() != null ? entity.getCliente().getId() : null)
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .puntoMuestreo(entity.getPuntoMuestreo())
                .fechaIngreso(entity.getFechaIngreso() != null ? entity.getFechaIngreso().toString() : null)
                .fechaEntrega(entity.getFechaEntrega() != null ? entity.getFechaEntrega().toString() : null)
                .observaciones(entity.getObservaciones())
                .matrizNombre(entity.getMatriz() != null ? entity.getMatriz().getNombre() : null)
                .matrizId(entity.getMatriz() != null ? entity.getMatriz().getId() : null)
                .tipoMuestraId(entity.getTipoMuestra() != null ? entity.getTipoMuestra().getId() : null)
                .resolucionesAplicadas(resoluciones)
                .parametros(parametros)
                .build();
    }

    private static ParametroResultadoTO mapParametroToResultadoTO(AnalisisParametroDE ap) {
        List<LimiteAplicableTO> limites = ap.getLimites() == null
                ? List.of()
                : ap.getLimites().stream().map(l -> {
            ResolucionDestinoDE destino = l.getLimiteOrigen().getDestino();
            String origenNombre = destino.getResolucion().getNombre() + " - " + destino.getNombre();

            return LimiteAplicableTO.builder()
                    .origenNombre(origenNombre)
                    .tipoLimite(l.getLimiteOrigen().getTipoLimite())
                    .limiteMin(l.getLimiteMin())
                    .limiteMax(l.getLimiteMax())
                    .limiteTexto(l.getLimiteTexto())
                    .cumple(l.getCumple())
                    .build();
        }).toList();

        return ParametroResultadoTO.builder()
                .id(ap.getParametro().getId())
                .nombre(ap.getParametro().getNombre())
                .unidad(ap.getLimites() == null ? null : ap.getLimites().stream()
                        .map(l -> l.getLimiteOrigen().getUnidad())
                        .filter(u -> u != null && !u.isBlank())
                        .findFirst().orElse(null))
                .metodologiaNombre(ap.getMetodologiaUsada() != null ? ap.getMetodologiaUsada().getNombre() : null)
                .valorResultado(ap.getValorResultado())
                .observacion(ap.getObservacion())
                .limites(limites)
                .build();
    }

    private static String resolverNombreCliente(AnalisisDE entity) {
        ClienteDE cliente = entity.getCliente();
        String clienteStr = null;
        if (cliente != null) {
            if (cliente.getTipoCliente() != null && cliente.getTipoCliente().name().equals("PERSONA_FISICA")) {
                clienteStr = (cliente.getNombre() != null ? cliente.getNombre() : "")
                        + (cliente.getApellido() != null ? " " + cliente.getApellido() : "");
            } else {
                clienteStr = cliente.getRazonSocial();
            }
        }
        if (clienteStr == null || clienteStr.isBlank()) {
            if (entity.getUser() != null) {
                clienteStr = entity.getUser().getUsername() + "/" + entity.getUser().getEmail();
            } else {
                clienteStr = "";
            }
        }
        return clienteStr;
    }
}