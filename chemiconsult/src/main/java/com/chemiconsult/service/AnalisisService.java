package com.chemiconsult.service;

import com.chemiconsult.brevo.service.BrevoEmailService;
import com.chemiconsult.entity.*;
import com.chemiconsult.enums.EstadoMuestraEnum;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private final NumeradorService    numeradorService;
    private final BrevoEmailService   brevoEmailService;
    private final MetodologiaRepository metodologiaRepository;
    private final EstudiosMapper estudiosMapper;
    private final AnalisisArchivoRepository analisisArchivoRepository;
    private final ClienteContactoRepository contactoRepository;

    public List<AnalisisDE> getEstudios() {
        return analisisRepository.findAll();
    }

    @Transactional
    public List<EstudioTO> getEstudiosTO() {
        List<AnalisisDE> all = analisisRepository.findAll();
        LocalDate hoy = LocalDate.now();
        List<AnalisisDE> vencidas = all.stream()
                .filter(a -> a.getFechaEntrega() != null
                        && a.getFechaEntrega().isBefore(hoy)
                        && (EstadoMuestraEnum.PENDIENTE.equals(a.getEstado())
                         || EstadoMuestraEnum.EN_PROCESO.equals(a.getEstado())))
                .collect(Collectors.toList());
        if (!vencidas.isEmpty()) {
            vencidas.forEach(a -> {
                a.setEstado(EstadoMuestraEnum.DEMORADA);
                a.setUpdateDate(hoy);
            });
            analisisRepository.saveAll(vencidas);
            log.info("Marcadas {} muestra(s) como DEMORADA por fecha de entrega vencida.", vencidas.size());
        }
        List<EstudioTO> tos = all.stream()
                .map(estudiosMapper::mapEntityToEstudioTO)
                .collect(Collectors.toList());
        poblarTieneFactura(tos);
        return tos;
    }

    @Transactional(readOnly = true)
    public List<EstudioTO> getEstudiosByID(Long userId) {
        ClienteDE cliente = clienteRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    com.chemiconsult.entity.ClienteContactoDE contacto = contactoRepository.findByUser_Id(userId)
                            .orElseThrow(() -> new RuntimeException("No se encontró un cliente asociado a este usuario"));
                    return contacto.getCliente();
                });

        List<EstudioTO> tos = analisisRepository.findAllByCliente(cliente)
                .stream()
                .map(estudiosMapper::mapEntityToEstudioTO)
                .collect(Collectors.toList());
        poblarTieneFactura(tos);
        return tos;
    }

    private void poblarTieneFactura(List<EstudioTO> tos) {
        if (tos.isEmpty()) return;
        List<Long> ids = tos.stream().map(EstudioTO::getId).toList();
        Set<Long> conFactura = analisisArchivoRepository
                .findAllByAnalisisIdInAndTipo(ids, "FACTURA")
                .stream()
                .map(a -> a.getAnalisis().getId())
                .collect(Collectors.toSet());
        tos.forEach(t -> t.setTieneFactura(conFactura.contains(t.getId())));
    }

    public Optional<AnalisisDE> getEstudio(Long id) {
        return this.analisisRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public AnalisisDetalleTO getEstudioDetalle(Long id) {
        AnalisisDE analisis = analisisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Muestra no encontrada con ID: " + id));
        return estudiosMapper.mapEntityToDetalleTO(analisis);
    }

    @Transactional
    public AnalisisDE createEstudio(EstudioTO estudio) {

        boolean manuallProvided = estudio.getNroProtocolo() != null && !estudio.getNroProtocolo().isBlank();
        String nroProtocolo = manuallProvided
                ? estudio.getNroProtocolo()
                : String.valueOf(numeradorService.generarSiguiente("NUMERO_PROTOCOLO"));
        estudio.setNroProtocolo(nroProtocolo);

        if (analisisRepository.existsByNumeroProtocolo(nroProtocolo)) {
            throw new RuntimeException("Ya existe una muestra con el protocolo: " + nroProtocolo);
        }

        if (manuallProvided) {
            try {
                numeradorService.sincronizarSiMayor("NUMERO_PROTOCOLO", Long.parseLong(nroProtocolo));
            } catch (NumberFormatException ignored) {
                // protocolo no numérico — no aplica sincronización
            }
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

        AnalisisDE analisis = estudiosMapper.createEstudio(estudio, cliente, matriz, sucursal);
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

        AnalisisDE guardado = analisisRepository.save(analisis);
        notificarAnalistas(guardado);
        return guardado;
    }

    private void notificarAnalistas(AnalisisDE analisis) {
        Map<Long, List<AnalisisParametroDE>> porAnalista = analisis.getParametros().stream()
                .filter(ap -> ap.getParametro().getResponsable() != null)
                .collect(Collectors.groupingBy(ap -> ap.getParametro().getResponsable().getId()));

        porAnalista.forEach((uid, params) -> {
            UserDE analista = params.get(0).getParametro().getResponsable();
            String listaParams = params.stream()
                    .map(ap -> ap.getParametro().getNombre())
                    .collect(Collectors.joining(", "));
            String html = buildEmailCola(
                    analista.getUsername(),
                    analisis.getNumeroProtocolo(),
                    listaParams,
                    analisis.getFechaEntrega());
            brevoEmailService.enviarMail(
                    analista.getEmail(),
                    analista.getUsername(),
                    "Nueva muestra asignada — #" + analisis.getNumeroProtocolo(),
                    html);
        });
    }

    private String buildEmailCola(String nombre, String protocolo, String params, LocalDate fechaEntrega) {
        String fecha = fechaEntrega != null
                ? fechaEntrega.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "AR")))
                : "Sin fecha definida";
        return """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:0 auto;background:#f8f9fa;padding:24px;border-radius:12px;">
              <div style="background:#2a8c5e;border-radius:8px 8px 0 0;padding:20px 24px;">
                <p style="color:#fff;margin:0;font-size:18px;font-weight:bold;">Chemiconsult · Nueva muestra asignada</p>
              </div>
              <div style="background:#fff;padding:24px;border-radius:0 0 8px 8px;border:1px solid #e0e0e0;border-top:none;">
                <p style="color:#333;margin:0 0 16px;">Hola <strong>%s</strong>,</p>
                <p style="color:#333;margin:0 0 16px;">Se ingresó la muestra <strong>#%s</strong> con parámetros que tenés asignados:</p>
                <div style="background:#f0f9f4;border-left:4px solid #2a8c5e;border-radius:4px;padding:14px 16px;margin:0 0 16px;">
                  <p style="margin:0 0 6px;color:#555;font-size:13px;">Parámetros a analizar</p>
                  <p style="margin:0;color:#1a5c3e;font-weight:bold;font-size:15px;">%s</p>
                </div>
                <div style="background:#f8f9fa;border-radius:6px;padding:12px 16px;margin:0 0 16px;">
                  <p style="margin:0;color:#555;font-size:13px;">📅 Fecha de entrega: <strong>%s</strong></p>
                </div>
                <p style="color:#888;font-size:12px;margin:0;">Ingresá a la app para ver el detalle completo y cargar tus resultados.</p>
              </div>
            </div>""".formatted(nombre, protocolo, params, fecha);
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
                             String puntoMuestreo, String fechaIngreso, String fechaEntrega,
                             List<Long> resolucionDestinoIds, List<Long> parametrosIds) {
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

        if (resolucionDestinoIds != null) {
            analisis.getResolucionesAplicadas().clear();
            for (Long destinoId : resolucionDestinoIds) {
                ResolucionDestinoDE destino = resolucionDestinoRepository.findById(destinoId)
                        .orElseThrow(() -> new RuntimeException("Destino regulatorio no encontrado: " + destinoId));
                AnalisisResolucionDestinoDE ard = new AnalisisResolucionDestinoDE();
                ard.setAnalisis(analisis);
                ard.setResolucionDestino(destino);
                analisis.getResolucionesAplicadas().add(ard);
            }
        }

        if (parametrosIds != null) {
            List<Long> destinoIdsParaLimites = resolucionDestinoIds != null ? resolucionDestinoIds :
                    analisis.getResolucionesAplicadas().stream()
                            .map(ard -> ard.getResolucionDestino().getId()).toList();

            Map<Long, AnalisisParametroDE> existingByParamId = analisis.getParametros().stream()
                    .collect(Collectors.toMap(ap -> ap.getParametro().getId(), ap -> ap));

            analisis.getParametros().clear();

            for (Long parametroId : parametrosIds) {
                ParametroDE parametro = parametroRepository.findById(parametroId)
                        .orElseThrow(() -> new RuntimeException("Parámetro no encontrado: " + parametroId));

                AnalisisParametroDE ap = new AnalisisParametroDE();
                ap.setAnalisis(analisis);
                ap.setParametro(parametro);

                AnalisisParametroDE old = existingByParamId.get(parametroId);
                if (old != null) {
                    ap.setValorResultado(old.getValorResultado());
                    ap.setObservacion(old.getObservacion());
                }

                List<ResolucionDestinoParametroDE> limitesEncontrados = destinoIdsParaLimites.isEmpty()
                        ? List.of()
                        : resolucionDestinoParametroRepository.findByDestinoIdsAndParametroId(destinoIdsParaLimites, parametroId);

                List<AnalisisParametroLimiteDE> limites = new ArrayList<>();
                for (ResolucionDestinoParametroDE origen : limitesEncontrados) {
                    AnalisisParametroLimiteDE limite = new AnalisisParametroLimiteDE();
                    limite.setAnalisisParametro(ap);
                    limite.setLimiteOrigen(origen);
                    limite.setLimiteMin(origen.getValorMinimo());
                    limite.setLimiteMax(origen.getValorMaximo());
                    limite.setLimiteTexto(origen.getLimiteTexto());
                    if (old != null && old.getValorResultado() != null && !old.getValorResultado().isBlank()) {
                        limite.setCumple(evaluarCumple(old.getValorResultado(), limite));
                    }
                    limites.add(limite);
                    if (ap.getMetodologiaUsada() == null) {
                        ap.setMetodologiaUsada(origen.getMetodologiaEstandar());
                    }
                }
                ap.setLimites(limites);
                analisis.getParametros().add(ap);
            }
        }

        analisis.setUpdateDate(LocalDate.now());
        analisisRepository.save(analisis);
    }

    public void deleteEstudio(Long id) {
        analisisRepository.deleteById(id);
    }

    @Transactional
    public void cancelarEstudio(Long id, String motivo) {
        AnalisisDE analisis = analisisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Muestra no encontrada: " + id));
        analisis.setEstado(EstadoMuestraEnum.CANCELADO);
        if (motivo != null && !motivo.isBlank()) {
            analisis.setObservaciones(motivo.trim());
        }
        analisis.setUpdateDate(LocalDate.now());
        analisisRepository.save(analisis);
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
            if (r.getMetodologiaId() != null) {
                metodologiaRepository.findById(r.getMetodologiaId())
                        .ifPresent(ap::setMetodologiaUsada);
            }
            for (AnalisisParametroLimiteDE limite : ap.getLimites()) {
                limite.setCumple(evaluarCumple(r.getValorResultado(), limite));
            }
        }

        recalcularEstadoDesdeCondiciones(analisis);
        analisis.setUpdateDate(LocalDate.now());
        analisisRepository.save(analisis);
    }

    @Transactional
    public void confirmarInforme(Long analisisId) {
        AnalisisDE analisis = analisisRepository.findById(analisisId)
                .orElseThrow(() -> new RuntimeException("Estudio no encontrado: " + analisisId));
        if (analisis.getEstado() != EstadoMuestraEnum.INFORME_PENDIENTE_REVISION) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "El informe no está en estado pendiente de revisión");
        }
        analisis.setEstado(EstadoMuestraEnum.COMPLETO);
        analisis.setUpdateDate(LocalDate.now());
        analisisRepository.save(analisis);
    }

    @Transactional
    public void invalidarInforme(Long analisisId) {
        AnalisisDE analisis = analisisRepository.findById(analisisId)
                .orElseThrow(() -> new RuntimeException("Estudio no encontrado: " + analisisId));
        if (analisis.getArchivos() != null) {
            analisis.getArchivos().stream()
                    .filter(a -> "INFORME".equalsIgnoreCase(a.getTipo()))
                    .forEach(a -> a.setDesactualizado(true));
        }
        recalcularEstadoDesdeCondiciones(analisis);
        analisis.setUpdateDate(LocalDate.now());
        analisisRepository.save(analisis);
    }

    @Transactional
    public void recalcularEstadoDesdeCondiciones(Long analisisId) {
        AnalisisDE analisis = analisisRepository.findById(analisisId)
                .orElseThrow(() -> new RuntimeException("Estudio no encontrado: " + analisisId));
        recalcularEstadoDesdeCondiciones(analisis);
        analisisRepository.save(analisis);
    }

    public void recalcularEstadoDesdeCondiciones(AnalisisDE analisis) {
        if (analisis == null || analisis.getEstado() == EstadoMuestraEnum.CANCELADO) {
            return;
        }

        boolean tieneParametros = analisis.getParametros() != null && !analisis.getParametros().isEmpty();
        boolean todosResultadosCargados = tieneParametros && analisis.getParametros().stream()
                .allMatch(ap -> ap.getValorResultado() != null && !ap.getValorResultado().trim().isEmpty());

        boolean tieneInforme = analisis.getArchivos() != null && analisis.getArchivos().stream()
                .filter(a -> a != null && "INFORME".equalsIgnoreCase(a.getTipo()))
                .anyMatch(a -> !Boolean.TRUE.equals(a.getDesactualizado()));

        if (!todosResultadosCargados) {
            analisis.setEstado(EstadoMuestraEnum.EN_PROCESO);
            return;
        }

        if (tieneInforme) {
            // Si el informe está pendiente de revisión, no lo pisamos automáticamente
            if (analisis.getEstado() != EstadoMuestraEnum.INFORME_PENDIENTE_REVISION) {
                analisis.setEstado(EstadoMuestraEnum.COMPLETO);
            }
        } else {
            analisis.setEstado(EstadoMuestraEnum.COMPLETO_SIN_INFORME);
        }
    }

    private Boolean evaluarCumple(String valorStr, AnalisisParametroLimiteDE limite) {
        if (valorStr == null || valorStr.isBlank()) return null;
        String tipo = limite.getLimiteOrigen().getTipoLimite();
        String limiteTexto = limite.getLimiteOrigen().getLimiteTexto();
        boolean esAusencia = "AUSENCIA".equals(tipo) ||
                ("TEXTO".equals(tipo) && "ausente".equalsIgnoreCase(limiteTexto));
        if (esAusencia) {
            String v = valorStr.trim().toLowerCase();
            if (v.equals("ausente")) return true;
            if (v.equals("presente")) return false;
            try { Double.parseDouble(valorStr.replace(",", ".")); return false; }
            catch (NumberFormatException ignored) { return null; }
        }
        if ("TEXTO".equals(tipo)) {
            // Texto con formato "X/Y" se trata como rango
            String txt = limite.getLimiteOrigen().getLimiteTexto();
            double[] rango = parsearRangoTexto(txt);
            if (rango == null) return null;
            try {
                double val = Double.parseDouble(valorStr.replace(",", ".").trim());
                return val >= rango[0] && val <= rango[1];
            } catch (NumberFormatException e) { return null; }
        }
        String vStr = valorStr.replace(",", ".").trim();
        // Resultados con prefijo de comparación (<0.05, <=0.05, >5, >=5)
        boolean esMenor  = vStr.startsWith("<=") || vStr.startsWith("<");
        boolean esMayor  = !esMenor && (vStr.startsWith(">=") || vStr.startsWith(">"));
        if (esMenor || esMayor) {
            String numStr = vStr.replaceFirst("^[<>]=?", "").trim();
            try {
                double umbral = Double.parseDouble(numStr);
                Double sMin = parseLimit(limite.getLimiteMin() != null ? limite.getLimiteMin() : limite.getLimiteOrigen().getValorMinimo());
                Double sMax = parseLimit(limite.getLimiteMax() != null ? limite.getLimiteMax() : limite.getLimiteOrigen().getValorMaximo());
                if (sMin == null || sMax == null) {
                    double[] rango = parsearRangoTexto(limite.getLimiteOrigen().getLimiteTexto());
                    if (rango != null) { sMin = rango[0]; sMax = rango[1]; }
                }
                if (esMenor) {
                    // valor real < umbral: si umbral <= sMax, el valor definitivamente cumple el MAX
                    return "MAX".equals(tipo) && sMax != null && umbral <= sMax ? true : null;
                } else {
                    // valor real > umbral: si umbral >= sMax, definitivamente NO cumple el MAX
                    if ("MAX".equals(tipo) && sMax != null && umbral >= sMax) return false;
                    // si umbral >= sMin, definitivamente cumple el MIN
                    if ("MIN".equals(tipo) && sMin != null && umbral >= sMin) return true;
                    return null;
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        try {
            double valor = Double.parseDouble(vStr);
            Double sMin = parseLimit(limite.getLimiteMin() != null ? limite.getLimiteMin() : limite.getLimiteOrigen().getValorMinimo());
            Double sMax = parseLimit(limite.getLimiteMax() != null ? limite.getLimiteMax() : limite.getLimiteOrigen().getValorMaximo());
            // Fallback: si min/max nulos, intentar parsear limiteTexto como "X/Y"
            if (sMin == null || sMax == null) {
                double[] rango = parsearRangoTexto(limite.getLimiteOrigen().getLimiteTexto());
                if (rango != null) { sMin = rango[0]; sMax = rango[1]; }
            }
            return switch (tipo) {
                case "MAX"   -> sMax != null && valor <= sMax;
                case "MIN"   -> sMin != null && valor >= sMin;
                case "RANGO" -> sMin != null && sMax != null && valor >= sMin && valor <= sMax;
                default      -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseLimit(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s.replace(",", ".")); }
        catch (NumberFormatException e) { return null; }
    }

    private static double[] parsearRangoTexto(String texto) {
        if (texto == null || !texto.contains("/")) return null;
        String[] parts = texto.replace(" ", "").split("/");
        if (parts.length != 2) return null;
        try {
            double min = Double.parseDouble(parts[0].replace(",", "."));
            double max = Double.parseDouble(parts[1].replace(",", "."));
            return new double[]{min, max};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Transactional
    public void guardarObservaciones(Long analisisId, String observaciones) {
        AnalisisDE analisis = analisisRepository.findById(analisisId)
                .orElseThrow(() -> new RuntimeException("Estudio no encontrado: " + analisisId));
        analisis.setObservaciones(observaciones);
        analisis.setUpdateDate(LocalDate.now());
        analisisRepository.save(analisis);
    }

    @Autowired
    public AnalisisService(AnalisisRepository analisisRepository,
                           ClienteRepository clienteRepository,
                           MatrizRepository matrizRepository,
                           ClienteSucursalRepository sucursalRepository,
                           ResolucionDestinoRepository resolucionDestinoRepository,
                           ParametroRepository parametroRepository,
                           ResolucionDestinoParametroRepository resolucionDestinoParametroRepository,
                           TipoMuestraRepository tipoMuestraRepository,
                           NumeradorService numeradorService,
                           BrevoEmailService brevoEmailService,
                           MetodologiaRepository metodologiaRepository,
                           EstudiosMapper estudiosMapper,
                           AnalisisArchivoRepository analisisArchivoRepository,
                           ClienteContactoRepository contactoRepository) {
        this.analisisRepository = analisisRepository;
        this.clienteRepository = clienteRepository;
        this.matrizRepository = matrizRepository;
        this.sucursalRepository = sucursalRepository;
        this.resolucionDestinoRepository = resolucionDestinoRepository;
        this.parametroRepository = parametroRepository;
        this.resolucionDestinoParametroRepository = resolucionDestinoParametroRepository;
        this.tipoMuestraRepository = tipoMuestraRepository;
        this.numeradorService = numeradorService;
        this.brevoEmailService = brevoEmailService;
        this.metodologiaRepository = metodologiaRepository;
        this.estudiosMapper = estudiosMapper;
        this.analisisArchivoRepository = analisisArchivoRepository;
        this.contactoRepository = contactoRepository;
    }
}