package com.chemiconsult.mapper;

import com.chemiconsult.entity.*;
import com.chemiconsult.enums.EstadoMuestraEnum;
import com.chemiconsult.repository.ParametroMetodologiaRepository;
import com.chemiconsult.to.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class EstudiosMapper {

    @Autowired
    private ParametroMetodologiaRepository pmRepository;

    public AnalisisDE createEstudio(EstudioTO estudio, ClienteDE cliente, MatrizDE matriz, ClienteSucursalDE sucursal) {
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

    public EstudioTO mapEntityToEstudioTO(AnalisisDE entity) {
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
                .tipoMuestraNombre(entity.getTipoMuestra() != null ? entity.getTipoMuestra().getNombre() : null)
                .build();
    }

    public AnalisisDetalleTO mapEntityToDetalleTO(AnalisisDE entity) {
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
                .map(this::mapParametroToResultadoTO)
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
                .resolucionDestinoIds(entity.getResolucionesAplicadas() == null ? List.of() :
                        entity.getResolucionesAplicadas().stream()
                                .map(ard -> ard.getResolucionDestino().getId())
                                .toList())
                .parametros(parametros)
                .build();
    }

    private ParametroResultadoTO mapParametroToResultadoTO(AnalisisParametroDE ap) {
        List<LimiteAplicableTO> limites = ap.getLimites() == null
                ? List.of()
                : ap.getLimites().stream().map(l -> {
            ResolucionDestinoDE destino = l.getLimiteOrigen().getDestino();
            String origenNombre = destino.getResolucion().getNombre() + " - " + destino.getNombre();

            ResolucionDestinoParametroDE origen = l.getLimiteOrigen();
            return LimiteAplicableTO.builder()
                    .origenNombre(origenNombre)
                    .tipoLimite(origen.getTipoLimite())
                    .limiteMin(l.getLimiteMin() != null ? l.getLimiteMin() : origen.getValorMinimo())
                    .limiteMax(l.getLimiteMax() != null ? l.getLimiteMax() : origen.getValorMaximo())
                    .limiteTexto(l.getLimiteTexto() != null ? l.getLimiteTexto() : origen.getLimiteTexto())
                    .cumple(l.getCumple())
                    .build();
        }).toList();

        return ParametroResultadoTO.builder()
                .id(ap.getParametro().getId())
                .nombre(ap.getParametro().getNombre())
                .unidad(resolverUnidad(ap))
                .tipoAnalisis(resolverTipoAnalisis(ap))
                .metodologiaId(ap.getMetodologiaUsada() != null ? ap.getMetodologiaUsada().getId() : null)
                .metodologiaNombre(ap.getMetodologiaUsada() != null ? ap.getMetodologiaUsada().getNombre() : null)
                .valorResultado(ap.getValorResultado())
                .observacion(ap.getObservacion())
                .limites(limites)
                .build();
    }

    /**
     * Resuelve el grupo del parámetro para el informe:
     * 1. tipoAnalisis en RESOL_DESTINO_PARAM (por resolución aplicada)
     * 2. tipoAnalisis en PARAMETRO_METODOLOGIA (por metodología analítica)
     * 3. Fallback: tipoAnalisis del parámetro
     */
    private String resolverTipoAnalisis(AnalisisParametroDE ap) {
        if (ap.getParametro() == null) return null;

        // 1. Desde los límites de la resolución aplicada
        if (ap.getLimites() != null) {
            String tipo = ap.getLimites().stream()
                    .map(l -> l.getLimiteOrigen().getTipoAnalisis())
                    .filter(t -> t != null && !t.isBlank())
                    .findFirst().orElse(null);
            if (tipo != null) return tipo;
        }

        // 2. Desde la asociación parámetro-metodología
        if (ap.getMetodologiaUsada() != null) {
            var pm = pmRepository.findByParametroIdAndMetodologiaId(
                    ap.getParametro().getId(), ap.getMetodologiaUsada().getId());
            if (pm.isPresent() && pm.get().getTipoAnalisis() != null
                    && !pm.get().getTipoAnalisis().isBlank()) {
                return pm.get().getTipoAnalisis();
            }
        }

        // 3. Fallback: tipoAnalisis del parámetro
        return ap.getParametro().getTipoAnalisis();
    }

    private static String resolverUnidad(AnalisisParametroDE ap) {
        if (ap.getLimites() != null) {
            String u = ap.getLimites().stream()
                    .map(l -> l.getLimiteOrigen().getUnidad())
                    .filter(s -> s != null && !s.isBlank())
                    .findFirst().orElse(null);
            if (u != null) return u;
        }
        return ap.getParametro() != null ? ap.getParametro().getUnidad() : null;
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
