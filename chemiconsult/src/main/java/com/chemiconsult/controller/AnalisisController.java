package com.chemiconsult.controller;

import com.chemiconsult.entity.AnalisisArchivoDE;
import com.chemiconsult.entity.AnalisisDE;
import com.chemiconsult.enums.EstadoMuestraEnum;
import com.chemiconsult.repository.AnalisisArchivoRepository;
import com.chemiconsult.repository.AnalisisRepository;
import com.chemiconsult.service.AnalisisService;
import com.chemiconsult.service.InformeService;
import com.chemiconsult.supabase.service.SupabaseBucketService;
import com.chemiconsult.to.AnalisisArchivoTO;
import com.chemiconsult.to.AnalisisDetalleTO;
import com.chemiconsult.to.EstudioTO;
import com.chemiconsult.to.ResultadoParametroTO;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log4j2
@RestController
@RequestMapping("/api/estudios")
public class AnalisisController {

    private final AnalisisService analisisService;
    private final SupabaseBucketService supabaseBucketService;
    private final AnalisisRepository analisisRepository;
    private final AnalisisArchivoRepository analisisArchivoRepository;
    private final InformeService informeService;
    private final String BUCKET = "chemiconsult-bucket";

    @GetMapping
    public List<AnalisisDE> getEstudios() {
        return analisisService.getEstudios();
    }

    @GetMapping("/all")
    public List<EstudioTO> getEstudiosTO() {
        return analisisService.getEstudiosTO();
    }

    @GetMapping("/user/{userId}")
    public List<EstudioTO> getEstudiosByID(@PathVariable Long userId) {
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
                        .build())
                .toList();
    }

    /** Descarga un archivo específico por su id. */
    @GetMapping("/{id}/archivos/{archivoId}")
    public ResponseEntity<byte[]> descargarArchivo(
            @PathVariable Long id,
            @PathVariable Long archivoId) {

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
            @RequestParam("file") MultipartFile file) {

        AnalisisDE analisis = analisisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String path = id + "/" + file.getOriginalFilename();
        supabaseBucketService.subirArchivo(BUCKET, path, file);

        AnalisisArchivoDE archivo = new AnalisisArchivoDE();
        archivo.setAnalisis(analisis);
        archivo.setArchivoUrl(path);
        archivo.setNombre(file.getOriginalFilename());
        archivo.setCreatedAt(LocalDate.now());
        analisisArchivoRepository.save(archivo);

        analisis.setEstado(EstadoMuestraEnum.COMPLETO);
        analisis.setUpdateDate(LocalDate.now());
        analisisRepository.save(analisis);

        return ResponseEntity.ok(AnalisisArchivoTO.builder()
                .id(archivo.getId())
                .nombre(archivo.getNombre())
                .createdAt(archivo.getCreatedAt().toString())
                .build());
    }

    /** Elimina un archivo específico de la muestra (storage + DB). */
    @DeleteMapping("/{id}/archivos/{archivoId}")
    @Transactional
    public ResponseEntity<Void> eliminarArchivo(
            @PathVariable Long id,
            @PathVariable Long archivoId) {

        AnalisisArchivoDE archivo = analisisArchivoRepository.findByIdAndAnalisisId(archivoId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        supabaseBucketService.eliminarArchivo(BUCKET, archivo.getArchivoUrl());
        analisisArchivoRepository.delete(archivo);

        return ResponseEntity.noContent().build();
    }

    /** Devuelve el primer archivo de la muestra (usado por el dashboard del cliente). */
    @GetMapping("/{id}/resultado")
    public ResponseEntity<byte[]> getResultado(@PathVariable Long id) {
        log.info("Obteniendo resultado del estudio con ID: {}", id);

        if (!analisisRepository.existsById(id)) {
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

    @PostMapping("/{id}/generar-informe")
    public ResponseEntity<byte[]> generarInforme(@PathVariable Long id) {
        AnalisisDE analisis = analisisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String nro = analisis.getNumeroProtocolo() != null ? analisis.getNumeroProtocolo() : String.valueOf(id);
        byte[] pdf = informeService.generarYPublicar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"informe-" + nro + ".pdf\"")
                .body(pdf);
    }

    @Autowired
    public AnalisisController(AnalisisService analisisService,
                              SupabaseBucketService supabaseBucketService,
                              AnalisisRepository analisisRepository,
                              AnalisisArchivoRepository analisisArchivoRepository,
                              InformeService informeService) {
        this.analisisService = analisisService;
        this.supabaseBucketService = supabaseBucketService;
        this.analisisRepository = analisisRepository;
        this.analisisArchivoRepository = analisisArchivoRepository;
        this.informeService = informeService;
    }
}
