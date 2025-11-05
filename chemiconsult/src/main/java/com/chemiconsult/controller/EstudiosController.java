package com.chemiconsult.controller;

import com.chemiconsult.entity.EstudiosDE;
import com.chemiconsult.service.EstudiosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estudios")
@CrossOrigin(origins = "*") // permite llamadas desde el frontend
public class EstudiosController {

    @Autowired
    EstudiosService estudiosService;

    @GetMapping
    public List<EstudiosDE> getEstudios() {
        return estudiosService.getEstudios();
    }

    @PostMapping
    public EstudiosDE createEstudio(@RequestBody EstudiosDE estudio) {
        return estudiosService.createEstudio(estudio);
    }

    @PutMapping("/{id}")
    public EstudiosDE updateEstudio(@PathVariable Long id, @RequestBody EstudiosDE estudio) {
        return estudiosService.updateEstudio(id, estudio);
    }

    @DeleteMapping("/{id}")
    public void deleteEstudio(@PathVariable Long id) {
        estudiosService.deleteEstudio(id);
    }
}