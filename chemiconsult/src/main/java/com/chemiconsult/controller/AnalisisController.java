package com.chemiconsult.controller;

import com.chemiconsult.entity.AnalisisDE;
import com.chemiconsult.enums.EstadoMuestraEnum;
import com.chemiconsult.repository.AnalisisRepository;
import com.chemiconsult.service.AnalisisService;
import com.chemiconsult.supabase.service.SupabaseBucketService;
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
import java.util.Optional;

@Log4j2
@RestController
@RequestMapping("/api/estudios")
@CrossOrigin(origins = "*")
public class AnalisisController {

    private final AnalisisService analisisService;
    private final SupabaseBucketService supabaseBucketService;
    private final AnalisisRepository analisisRepository;
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
    public ResponseEntity<Void> createEstudio(@RequestBody EstudioTO estudio) {
        analisisService.createEstudio(estudio);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    @Transactional
    public AnalisisDE updateEstudio(@PathVariable Long id, @RequestBody AnalisisDE estudio) {
        return analisisService.updateEstudio(id, estudio);
    }

    @DeleteMapping("/{id}")
    public void deleteEstudio(@PathVariable Long id) {
        analisisService.deleteEstudio(id);
    }

    @PutMapping("/{id}/resultados")
    public ResponseEntity<Void> guardarResultados(
            @PathVariable Long id,
            @RequestBody List<ResultadoParametroTO> resultados) {
        analisisService.guardarResultados(id, resultados);
        return ResponseEntity.ok().build();
    }

    // Descarga el PDF desde Supabase (ya no hay fallback a bytea local)
    @GetMapping("/{id}/resultado")
    public ResponseEntity<byte[]> getResultado(@PathVariable Long id) {
        log.info("Obteniendo resultado del estudio con ID: {}", id);

        AnalisisDE analisis = analisisService.getEstudio(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String path = analisis.getArchivoUrl();
        if (path == null || path.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        byte[] archivo = supabaseBucketService.descargarArchivo(BUCKET, path);
        String nombreArchivo = path.substring(path.lastIndexOf('/') + 1);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreArchivo + "\"")
                .body(archivo);
    }

    @PostMapping("/{id}/documento")
    public ResponseEntity<Void> subirDocumento(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        AnalisisDE analisis = analisisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String path = id + "/" + file.getOriginalFilename();
        supabaseBucketService.subirArchivo(BUCKET, path, file);

        analisis.setArchivoUrl(path);
        analisis.setEstado(EstadoMuestraEnum.COMPLETO);
        analisis.setUpdateDate(LocalDate.now());
        analisisRepository.save(analisis);

        return ResponseEntity.ok().build();
    }

    @Autowired
    public AnalisisController(AnalisisService analisisService,
                              SupabaseBucketService supabaseBucketService,
                              AnalisisRepository analisisRepository) {
        this.analisisService = analisisService;
        this.supabaseBucketService = supabaseBucketService;
        this.analisisRepository = analisisRepository;
    }
}