package com.chemiconsult.service;

import com.chemiconsult.entity.*;
import com.chemiconsult.mapper.EstudiosMapper;
import com.chemiconsult.repository.*;
import com.chemiconsult.to.AnalisisDetalleTO;
import com.chemiconsult.to.EstudioTO;
import com.chemiconsult.to.ResultadoParametroTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
public class AnalisisService {

    private final AnalisisRepository analisisRepository;
    private final ClienteRepository clienteRepository;
    private final MatrizRepository matrizRepository;
    private final ClienteSucursalRepository sucursalRepository;
    private final ResolucionDestinoRepository resolucionDestinoRepository;
    private final ParametroRepository parametroRepository;
    private final ResolucionDestinoParametroRepository resolucionDestinoParametroRepository;
    private final TipoMuestraRepository tipoMuestraRepository;

    public List<AnalisisDE> getEstudios() {
        return analisisRepository.findAll();
    }

    public List<EstudioTO> getEstudiosTO() {
        return analisisRepository.findAll()
                .stream()
                .map(EstudiosMapper::mapEntityToEstudioTO)
                .toList();
    }

    public List<EstudioTO> getEstudiosByID(Long userId) {
        ClienteDE cliente = clienteRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("No se encontró un cliente asociado a este usuario"));

        return analisisRepository.findAllByCliente(cliente)
                .stream()
                .map(EstudiosMapper::mapEntityToEstudioTO)
                .toList();
    }

    public Optional<AnalisisDE> getEstudio(Long id) {
        return this.analisisRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public AnalisisDetalleTO getEstudioDetalle(Long id) {
        AnalisisDE analisis = analisisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Muestra no encontrada con ID: " + id));
        return EstudiosMapper.mapEntityToDetalleTO(analisis);
    }

    @Transactional
    public AnalisisDE createEstudio(EstudioTO estudio) {

        if (estudio.getNroProtocolo() == null || estudio.getNroProtocolo().isBlank()) {
            throw new RuntimeException("El número de protocolo es obligatorio");
        }
        if (analisisRepository.existsByNumeroProtocolo(estudio.getNroProtocolo())) {
            throw new RuntimeException("Ya existe una muestra con el protocolo: " + estudio.getNroProtocolo());
        }
        if (estudio.getParametrosIds() == null || estudio.getParametrosIds().isEmpty()) {
            throw new RuntimeException("Debe seleccionar al menos un parámetro a analizar");
        }
        if (estudio.getClienteId() == null) {
            throw new RuntimeException("Debe seleccionar un cliente");
        }

        ClienteDE cliente = clienteRepository.findById(estudio.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        MatrizDE matriz = matrizRepository.findById(estudio.getMatrizId())
                .orElseThrow(() -> new RuntimeException("Matriz no encontrada"));

        ClienteSucursalDE sucursal = null;
        if (estudio.getSucursalId() != null) {
            sucursal = sucursalRepository.findById(estudio.getSucursalId())
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

            if (!sucursal.getCliente().getId().equals(cliente.getId())) {
                throw new RuntimeException("La sucursal seleccionada no pertenece al cliente indicado");
            }
        }

        AnalisisDE analisis = EstudiosMapper.createEstudio(estudio, cliente, matriz, sucursal);
        if (estudio.getTipoMuestraId() != null) {
            tipoMuestraRepository.findById(estudio.getTipoMuestraId())
                    .ifPresent(analisis::setTipoMuestra);
        }
        analisis = analisisRepository.save(analisis);

        List<Long> destinoIds = estudio.getResolucionDestinoIds() != null
                ? estudio.getResolucionDestinoIds() : List.of();

        List<AnalisisResolucionDestinoDE> resolucionesAplicadas = new ArrayList<>();
        for (Long destinoId : destinoIds) {
            ResolucionDestinoDE destino = resolucionDestinoRepository.findById(destinoId)
                    .orElseThrow(() -> new RuntimeException("Destino regulatorio no encontrado: " + destinoId));

            AnalisisResolucionDestinoDE ard = new AnalisisResolucionDestinoDE();
            ard.setAnalisis(analisis);
            ard.setResolucionDestino(destino);
            resolucionesAplicadas.add(ard);
        }
        analisis.setResolucionesAplicadas(resolucionesAplicadas);

        List<AnalisisParametroDE> parametros = new ArrayList<>();
        for (Long parametroId : estudio.getParametrosIds()) {
            ParametroDE parametro = parametroRepository.findById(parametroId)
                    .orElseThrow(() -> new RuntimeException("Parámetro no encontrado: " + parametroId));

            AnalisisParametroDE ap = new AnalisisParametroDE();
            ap.setAnalisis(analisis);
            ap.setParametro(parametro);

            List<ResolucionDestinoParametroDE> limitesEncontrados = destinoIds.isEmpty()
                    ? List.of()
                    : resolucionDestinoParametroRepository.findByDestinoIdsAndParametroId(destinoIds, parametroId);

            List<AnalisisParametroLimiteDE> limites = new ArrayList<>();
            for (ResolucionDestinoParametroDE origen : limitesEncontrados) {
                AnalisisParametroLimiteDE limite = new AnalisisParametroLimiteDE();
                limite.setAnalisisParametro(ap);
                limite.setLimiteOrigen(origen);
                limite.setLimiteMin(origen.getValorMinimo());
                limite.setLimiteMax(origen.getValorMaximo());
                limite.setLimiteTexto(origen.getLimiteTexto());
                limites.add(limite);

                if (ap.getMetodologiaUsada() == null) {
                    ap.setMetodologiaUsada(origen.getMetodologiaEstandar());
                }
            }
            ap.setLimites(limites);

            parametros.add(ap);
        }
        analisis.setParametros(parametros);

        return analisisRepository.save(analisis);
    }

    public AnalisisDE updateEstudio(Long id, AnalisisDE estudio) {
        AnalisisDE existing = analisisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudio no encontrado con ID: " + id));

        if (estudio.getEstado() != null) existing.setEstado(estudio.getEstado());
        existing.setObservaciones(estudio.getObservaciones());
        existing.setUpdateDate(LocalDate.now());
        if (estudio.getUser() != null) existing.setUser(estudio.getUser());
        if (estudio.getMatriz() != null) existing.setMatriz(estudio.getMatriz());

        return analisisRepository.save(existing);
    }

    @Transactional
    public void patchEstudio(Long id, Long matrizId, Long tipoMuestraId,
                             String puntoMuestreo, String fechaIngreso, String fechaEntrega) {
        AnalisisDE analisis = analisisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Muestra no encontrada: " + id));
        if (matrizId != null) {
            matrizRepository.findById(matrizId).ifPresent(analisis::setMatriz);
        }
        if (tipoMuestraId != null) {
            tipoMuestraRepository.findById(tipoMuestraId).ifPresent(analisis::setTipoMuestra);
        } else {
            analisis.setTipoMuestra(null);
        }
        analisis.setPuntoMuestreo(puntoMuestreo != null && !puntoMuestreo.isBlank() ? puntoMuestreo : null);
        if (fechaIngreso != null && !fechaIngreso.isBlank()) {
            analisis.setFechaIngreso(LocalDate.parse(fechaIngreso));
        }
        analisis.setFechaEntrega(fechaEntrega != null && !fechaEntrega.isBlank()
                ? LocalDate.parse(fechaEntrega) : null);
        analisis.setUpdateDate(LocalDate.now());
        analisisRepository.save(analisis);
    }

    public void deleteEstudio(Long id) {
        analisisRepository.deleteById(id);
    }

    @Transactional
    public void guardarResultados(Long analisisId, List<ResultadoParametroTO> resultados) {
        AnalisisDE analisis = analisisRepository.findById(analisisId)
                .orElseThrow(() -> new RuntimeException("Estudio no encontrado: " + analisisId));

        Map<Long, ResultadoParametroTO> porParametroId = resultados.stream()
                .collect(Collectors.toMap(ResultadoParametroTO::getParametroId, r -> r));

        for (AnalisisParametroDE ap : analisis.getParametros()) {
            ResultadoParametroTO r = porParametroId.get(ap.getParametro().getId());
            if (r == null) continue;
            ap.setValorResultado(r.getValorResultado());
            ap.setObservacion(r.getObservacion());
            for (AnalisisParametroLimiteDE limite : ap.getLimites()) {
                limite.setCumple(evaluarCumple(r.getValorResultado(), limite));
            }
        }

        analisis.setUpdateDate(LocalDate.now());
        analisisRepository.save(analisis);
    }

    private Boolean evaluarCumple(String valorStr, AnalisisParametroLimiteDE limite) {
        if (valorStr == null || valorStr.isBlank()) return null;
        String tipo = limite.getLimiteOrigen().getTipoLimite();
        if ("TEXTO".equals(tipo)) return null;
        try {
            double valor = Double.parseDouble(valorStr.replace(",", ".").trim());
            return switch (tipo) {
                case "MAX"   -> limite.getLimiteMax() != null && valor <= limite.getLimiteMax();
                case "MIN"   -> limite.getLimiteMin() != null && valor >= limite.getLimiteMin();
                case "RANGO" -> limite.getLimiteMin() != null && limite.getLimiteMax() != null
                                && valor >= limite.getLimiteMin() && valor <= limite.getLimiteMax();
                default      -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Autowired
    public AnalisisService(AnalisisRepository analisisRepository,
                           ClienteRepository clienteRepository,
                           MatrizRepository matrizRepository,
                           ClienteSucursalRepository sucursalRepository,
                           ResolucionDestinoRepository resolucionDestinoRepository,
                           ParametroRepository parametroRepository,
                           ResolucionDestinoParametroRepository resolucionDestinoParametroRepository,
                           TipoMuestraRepository tipoMuestraRepository) {
        this.analisisRepository = analisisRepository;
        this.clienteRepository = clienteRepository;
        this.matrizRepository = matrizRepository;
        this.sucursalRepository = sucursalRepository;
        this.resolucionDestinoRepository = resolucionDestinoRepository;
        this.parametroRepository = parametroRepository;
        this.resolucionDestinoParametroRepository = resolucionDestinoParametroRepository;
        this.tipoMuestraRepository = tipoMuestraRepository;
    }
}