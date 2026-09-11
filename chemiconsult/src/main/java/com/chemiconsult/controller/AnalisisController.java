package com.chemiconsult.controller;

import com.chemiconsult.entity.AnalisisArchivoDE;
import com.chemiconsult.entity.AnalisisDE;
import com.chemiconsult.entity.UserDE;
import com.chemiconsult.enums.EstadoMuestraEnum;
import com.chemiconsult.repository.AnalisisArchivoRepository;
import com.chemiconsult.repository.AnalisisRepository;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.service.AnalisisService;
import com.chemiconsult.service.ExportWordService;
import com.chemiconsult.service.InformeService;
import com.chemiconsult.supabase.service.SupabaseBucketService;
import com.chemiconsult.to.AnalisisArchivoTO;
import com.chemiconsult.to.AnalisisDetalleTO;
import com.chemiconsult.to.EstudioTO;
import com.chemiconsult.to.ResultadoParametroTO;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Log4j2
@RestController
@RequestMapping("/api/estudios")
public class AnalisisController {

    private final AnalisisService analisisService;
    private final SupabaseBucketService supabaseBucketService;
    private final AnalisisRepository analisisRepository;
    private final AnalisisArchivoRepository analisisArchivoRepository;
    private final InformeService informeService;
    private final ExportWordService exportWordService;
    private final UserRepository userRepository;
    private final String BUCKET = "chemiconsult-bucket";

    @GetMapping
    public List<AnalisisDE> getEstudios() {
        return analisisService.getEstudios();
    }

    @GetMapping("/all")
    public List<EstudioTO> getEstudiosTO() {
        return analisisService.getEstudiosTO();
    }

    @GetMapping("/activas")
    public List<EstudioTO> getEstudiosActivos() {
        return analisisService.getEstudiosActivos();
    }

    @GetMapping("/kpi-mes")
    public Map<String, Long> getKpiMes(@RequestParam int year, @RequestParam int month) {
        return analisisService.getKpiMes(year, month);
    }

    @GetMapping("/user/{userId}")
    public List<EstudioTO> getEstudiosByID(@PathVariable Long userId,
                                            @AuthenticationPrincipal UserDetails principal) {
        if (esCliente(principal)) {
            userId = resolveCallerUserId(principal);
        }
        return analisisService.getEstudiosByID(userId);
    }

    @GetMapping("/{id}")
    public Optional<AnalisisDE> getEstudio(@PathVariable Long id) {
        return analisisService.getEstudio(id);
    }

    @GetMapping("/{id}/detalle")
    public AnalisisDetalleTO getEstudioDetalle(@PathVariable Long id) {
        return analisisService.getEstudioDetalle(id);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Long> createEstudio(@RequestBody EstudioTO estudio) {
        AnalisisDE created = analisisService.createEstudio(estudio);
        return ResponseEntity.status(HttpStatus.CREATED).body(created.getId());
    }

    @PutMapping("/{id}")
    @Transactional
    public AnalisisDE updateEstudio(@PathVariable Long id, @RequestBody AnalisisDE estudio) {
        return analisisService.updateEstudio(id, estudio);
    }

    @SuppressWarnings("unchecked")
    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> patchEstudio(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long matrizId = body.get("matrizId") != null ? ((Number) body.get("matrizId")).longValue() : null;
        Long tipoMuestraId = body.get("tipoMuestraId") != null ? ((Number) body.get("tipoMuestraId")).longValue() : null;
        String puntoMuestreo = (String) body.get("puntoMuestreo");
        String fechaIngreso = (String) body.get("fechaIngreso");
        String fechaEntrega = (String) body.get("fechaEntrega");
        List<Long> resolucionDestinoIds = body.get("resolucionDestinoIds") != null
                ? ((List<Number>) body.get("resolucionDestinoIds")).stream().map(Number::longValue).toList()
                : null;
        List<Long> parametrosIds = body.get("parametrosIds") != null
                ? ((List<Number>) body.get("parametrosIds")).stream().map(Number::longValue).toList()
                : null;
        analisisService.patchEstudio(id, matrizId, tipoMuestraId, puntoMuestreo, fechaIngreso, fechaEntrega,
                resolucionDestinoIds, parametrosIds);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public void deleteEstudio(@PathVariable Long id) {
        analisisService.deleteEstudio(id);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarEstudio(@PathVariable Long id,
                                                @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.get("motivo") : null;
        analisisService.cancelarEstudio(id, motivo);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/resultados")
    public ResponseEntity<Void> guardarResultados(
            @PathVariable Long id,
            @RequestBody List<ResultadoParametroTO> resultados) {
        analisisService.guardarResultados(id, resultados);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/observaciones")
    @Transactional
    public ResponseEntity<Void> guardarObservaciones(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String observaciones = (String) body.get("observaciones");
        analisisService.guardarObservaciones(id, observaciones);
        return ResponseEntity.ok().build();
    }

    // ── ARCHIVOS ────────────────────────────────────────────────────────────────

    /** Lista todos los archivos asociados a una muestra. */
    @GetMapping("/{id}/archivos")
    public List<AnalisisArchivoTO> listarArchivos(@PathVariable Long id) {
        if (!analisisRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return analisisArchivoRepository.findAllByAnalisisIdOrderByCreatedAtAsc(id).stream()
                .map(a -> AnalisisArchivoTO.builder()
                        .id(a.getId())
                        .nombre(a.getNombre())
                        .createdAt(a.getCreatedAt() != null ? a.getCreatedAt().toString() : null)
                        .tipo(a.getTipo())
                        .desactualizado(a.getDesactualizado())
                        .build())
                .toList();
    }

    /** Confirma el informe pendiente de revisión (estado pasa a COMPLETO). */
    @PostMapping("/{id}/confirmar-informe")
    public ResponseEntity<Void> confirmarInforme(@PathVariable Long id) {
        analisisService.confirmarInforme(id);
        return ResponseEntity.ok().build();
    }

    /** Marca el informe de una muestra como desactualizado (estado pasa a COMPLETO_SIN_INFORME). */
    @PostMapping("/{id}/invalidar-informe")
    public ResponseEntity<Void> invalidarInforme(@PathVariable Long id) {
        analisisService.invalidarInforme(id);
        return ResponseEntity.ok().build();
    }

    /** Descarga un archivo específico por su id. */
    @GetMapping("/{id}/archivos/{archivoId}")
    public ResponseEntity<byte[]> descargarArchivo(
            @PathVariable Long id,
            @PathVariable Long archivoId,
            @AuthenticationPrincipal UserDetails principal) {

        if (esCliente(principal)) {
            analisisService.verificarAccesoEstudio(id, resolveCallerUserId(principal));
        }

        AnalisisArchivoDE archivo = analisisArchivoRepository.findByIdAndAnalisisId(archivoId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        byte[] bytes = supabaseBucketService.descargarArchivo(BUCKET, archivo.getArchivoUrl());
        String nombreArchivo = archivo.getNombre() != null ? archivo.getNombre()
                : archivo.getArchivoUrl().substring(archivo.getArchivoUrl().lastIndexOf('/') + 1);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreArchivo + "\"")
                .body(bytes);
    }

    /** Sube un archivo PDF y lo asocia a la muestra. */
    @PostMapping("/{id}/documento")
    @Transactional
    public ResponseEntity<AnalisisArchivoTO> subirDocumento(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tipo", defaultValue = "FACTURA") String tipo) {

        AnalisisDE analisis = analisisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String path = id + "/" + file.getOriginalFilename();
        supabaseBucketService.subirArchivo(BUCKET, path, file);

        AnalisisArchivoDE archivo = new AnalisisArchivoDE();
        archivo.setAnalisis(analisis);
        archivo.setArchivoUrl(path);
        archivo.setNombre(file.getOriginalFilename());
        archivo.setCreatedAt(LocalDate.now());
        archivo.setTipo(tipo.toUpperCase());
        analisisArchivoRepository.save(archivo);

        if (analisis.getArchivos() == null) {
            analisis.setArchivos(new java.util.ArrayList<>());
        }
        analisis.getArchivos().add(archivo);

        analisisService.recalcularEstadoDesdeCondiciones(analisis);
        analisis.setUpdateDate(LocalDate.now());
        analisisRepository.save(analisis);

        return ResponseEntity.ok(AnalisisArchivoTO.builder()
                .id(archivo.getId())
                .nombre(archivo.getNombre())
                .createdAt(archivo.getCreatedAt().toString())
                .tipo(archivo.getTipo())
                .build());
    }

    /** Elimina un archivo específico de la muestra (storage + DB). */
    @DeleteMapping("/{id}/archivos/{archivoId}")
    @Transactional
    public ResponseEntity<Void> eliminarArchivo(
            @PathVariable Long id,
            @PathVariable Long archivoId) {

        AnalisisDE analisis = analisisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        AnalisisArchivoDE archivo = analisis.getArchivos().stream()
                .filter(a -> a.getId().equals(archivoId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        supabaseBucketService.eliminarArchivo(BUCKET, archivo.getArchivoUrl());
        analisis.getArchivos().remove(archivo);
        analisisService.recalcularEstadoDesdeCondiciones(analisis);
        analisis.setUpdateDate(LocalDate.now());
        analisisRepository.save(analisis);

        return ResponseEntity.noContent().build();
    }

    /** Devuelve el primer archivo de la muestra (usado por el dashboard del cliente). */
    @GetMapping("/{id}/resultado")
    public ResponseEntity<byte[]> getResultado(@PathVariable Long id,
                                                @AuthenticationPrincipal UserDetails principal) {
        log.info("Obteniendo resultado del estudio con ID: {}", id);

        if (esCliente(principal)) {
            analisisService.verificarAccesoEstudio(id, resolveCallerUserId(principal));
        } else if (!analisisRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        List<AnalisisArchivoDE> archivos =
                analisisArchivoRepository.findAllByAnalisisIdOrderByCreatedAtAsc(id);

        if (archivos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String path = archivos.get(0).getArchivoUrl();
        byte[] bytes = supabaseBucketService.descargarArchivo(BUCKET, path);
        String nombreArchivo = archivos.get(0).getNombre() != null
                ? archivos.get(0).getNombre()
                : path.substring(path.lastIndexOf('/') + 1);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreArchivo + "\"")
                .body(bytes);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/{id}/generar-informe")
    public ResponseEntity<byte[]> generarInforme(@PathVariable Long id,
                                                  @RequestBody(required = false) Map<String, Object> body) {
        AnalisisDE analisis = analisisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String nro = analisis.getNumeroProtocolo() != null ? analisis.getNumeroProtocolo() : String.valueOf(id);

        List<Long> equipoIds = (body != null && body.get("equipoIds") != null)
                ? ((List<Number>) body.get("equipoIds")).stream().map(Number::longValue).toList()
                : List.of();

        boolean preview                = body != null && Boolean.TRUE.equals(body.get("preview"));
        boolean incluirConclusion      = body == null || !Boolean.FALSE.equals(body.get("incluirConclusion"));
        boolean incluirConclusionAuto  = body == null || !Boolean.FALSE.equals(body.get("incluirConclusionAuto"));

        byte[] pdf = informeService.generarYPublicar(id, equipoIds, preview, incluirConclusion, incluirConclusionAuto);

        String disposition = preview ? "inline" : "attachment";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"informe-" + nro + ".pdf\"")
                .body(pdf);
    }

    /** Descarga un único .docx con los datos completos de una muestra. */
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportarMuestra(@PathVariable Long id) {
        AnalisisDE analisis = analisisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Muestra no encontrada"));
        if (analisis.getEstado() != EstadoMuestraEnum.COMPLETO
                && analisis.getEstado() != EstadoMuestraEnum.COMPLETO_SIN_INFORME) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se pueden exportar muestras completas");
        }
        AnalisisDetalleTO detalle = analisisService.getEstudioDetalle(id);
        try {
            byte[] docx = exportWordService.generarDocx(detalle);
            String nombre = "muestra-" + nvlProtocolo(detalle) + ".docx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .body(docx);
        } catch (Exception e) {
            log.error("Error exportando muestra {} a Word", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al generar el archivo Word");
        }
    }

    /** Descarga un .zip con un .docx por cada muestra en el rango de fechas indicado. */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportarRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        List<AnalisisDE> muestras = analisisRepository
                .findAllByFechaIngresoBetweenOrderByFechaIngresoAsc(desde, hasta)
                .stream()
                .filter(m -> m.getEstado() == EstadoMuestraEnum.COMPLETO
                        || m.getEstado() == EstadoMuestraEnum.COMPLETO_SIN_INFORME)
                .toList();
        if (muestras.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No hay muestras completas en ese rango de fechas");
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(baos)) {

            for (AnalisisDE m : muestras) {
                AnalisisDetalleTO detalle = analisisService.getEstudioDetalle(m.getId());
                byte[] docx = exportWordService.generarDocx(detalle);
                String nombre = "muestra-" + nvlProtocolo(detalle) + ".docx";
                zip.putNextEntry(new ZipEntry(nombre));
                zip.write(docx);
                zip.closeEntry();
            }
            zip.finish();

            String zipNombre = "export_" + desde + "_" + hasta + ".zip";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipNombre + "\"")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(baos.toByteArray());
        } catch (Exception e) {
            log.error("Error exportando rango {} - {} a Word", desde, hasta, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al generar el archivo ZIP");
        }
    }

    private String nvlProtocolo(AnalisisDetalleTO d) {
        return (d.getNroProtocolo() != null && !d.getNroProtocolo().isBlank())
                ? d.getNroProtocolo().replaceAll("[^a-zA-Z0-9\\-_]", "_")
                : String.valueOf(d.getId());
    }

    private boolean esCliente(UserDetails principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
    }

    private Long resolveCallerUserId(UserDetails principal) {
        UserDE user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return user.getId();
    }

    @Autowired
    public AnalisisController(AnalisisService analisisService,
                              SupabaseBucketService supabaseBucketService,
                              AnalisisRepository analisisRepository,
                              AnalisisArchivoRepository analisisArchivoRepository,
                              InformeService informeService,
                              ExportWordService exportWordService,
                              UserRepository userRepository) {
        this.analisisService = analisisService;
        this.supabaseBucketService = supabaseBucketService;
        this.analisisRepository = analisisRepository;
        this.analisisArchivoRepository = analisisArchivoRepository;
        this.informeService = informeService;
        this.exportWordService = exportWordService;
        this.userRepository = userRepository;
    }
}
