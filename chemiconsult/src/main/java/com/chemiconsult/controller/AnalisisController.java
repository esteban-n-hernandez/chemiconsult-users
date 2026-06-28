package com.chemiconsult.controller;

import com.chemiconsult.entity.AnalisisDE;
import com.chemiconsult.repository.AnalisisRepository;
import com.chemiconsult.service.AnalisisService;
import com.chemiconsult.supabase.service.SupabaseBucketService;
import com.chemiconsult.to.EstudioTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/estudios")
@CrossOrigin(origins = "*") // permite llamadas desde el frontend
public class AnalisisController {

    @Autowired
    AnalisisService analisisService;

    @Autowired
    SupabaseBucketService supabaseBucketService;
    @Autowired
    private AnalisisRepository analisisRepository;

    private final String BUCKET = "chemiconsult-bucket";

    @GetMapping
    public List<AnalisisDE> getEstudios() {
        return analisisService.getEstudios();
    }

    @GetMapping("/user/{userId}")
    public List<EstudioTO> getEstudiosByID(@PathVariable Long userId) {
        return analisisService.getEstudiosByID(userId);
    }

    @GetMapping("/{id}")
    public Optional<AnalisisDE> getEstudio(@PathVariable Long id) {
        return analisisService.getEstudio(id);
    }

    @PostMapping
    public AnalisisDE createEstudio(@RequestBody EstudioTO estudio) {
        return analisisService.createEstudio(estudio);
    }

    @PutMapping("/{id}")
    public AnalisisDE updateEstudio(@PathVariable Long id, @RequestBody AnalisisDE estudio) {
        return analisisService.updateEstudio(id, estudio);
    }

    @DeleteMapping("/{id}")
    public void deleteEstudio(@PathVariable Long id) {
        analisisService.deleteEstudio(id);
    }

    @GetMapping("/{id}/resultado")
    public ResponseEntity<byte[]> getResultado(@PathVariable Long id) {
        Optional<AnalisisDE> estudio = analisisService.getEstudio(id);

        if (estudio.isPresent()) {
            if (estudio.get().getArchivo() == null) {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"resultado_" + id + ".pdf\"")
                .body(estudio.get().getArchivo());
    }

    @PostMapping("/{id}/documento")
    public ResponseEntity<Void> subirDocumento(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        AnalisisDE analisis = analisisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String path = id + "/informe.pdf";

        supabaseBucketService.subirArchivo(BUCKET, path, file);

        analisis.setArchivoUrl(path);
        analisis.setUpdateDate(LocalDate.now());
        analisisRepository.save(analisis);

        return ResponseEntity.ok().build();
    }

}