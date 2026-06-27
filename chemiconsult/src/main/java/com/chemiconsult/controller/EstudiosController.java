package com.chemiconsult.controller;

import com.chemiconsult.entity.AnalisisDE;
import com.chemiconsult.service.EstudiosService;
import com.chemiconsult.to.EstudioTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/estudios")
@CrossOrigin(origins = "*") // permite llamadas desde el frontend
public class EstudiosController {

    @Autowired
    EstudiosService estudiosService;

    @GetMapping
    public List<AnalisisDE> getEstudios() {
        return estudiosService.getEstudios();
    }

    @GetMapping("/user/{userId}")
    public List<EstudioTO> getEstudiosByID(@PathVariable Long userId) {
        return estudiosService.getEstudiosByID(userId);
    }

    @GetMapping("/{id}")
    public Optional<AnalisisDE> getEstudio(@PathVariable Long id) {
        return estudiosService.getEstudio(id);
    }

    @PostMapping
    public AnalisisDE createEstudio(@RequestBody EstudioTO estudio) {
        return estudiosService.createEstudio(estudio);
    }

    @PutMapping("/{id}")
    public AnalisisDE updateEstudio(@PathVariable Long id, @RequestBody AnalisisDE estudio) {
        return estudiosService.updateEstudio(id, estudio);
    }

    @DeleteMapping("/{id}")
    public void deleteEstudio(@PathVariable Long id) {
        estudiosService.deleteEstudio(id);
    }

    @GetMapping("/{id}/resultado")
    public ResponseEntity<byte[]> getResultado(@PathVariable Long id) {
        Optional<AnalisisDE> estudio = estudiosService.getEstudio(id);

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

}