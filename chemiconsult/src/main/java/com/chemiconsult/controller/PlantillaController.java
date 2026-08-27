package com.chemiconsult.controller;

import com.chemiconsult.entity.PlantillaDE;
import com.chemiconsult.service.PlantillaService;
import com.chemiconsult.to.PlantillaTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plantillas")
public class PlantillaController {

    private final PlantillaService plantillaService;

    @Autowired
    public PlantillaController(PlantillaService plantillaService) {
        this.plantillaService = plantillaService;
    }

    @GetMapping
    public List<PlantillaDE> getPlantillas() {
        return plantillaService.getPlantillas();
    }

    @GetMapping("/todas")
    public List<PlantillaDE> getPlantillasTodas() {
        return plantillaService.getPlantillasTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantillaDE> getPlantilla(@PathVariable Long id) {
        return plantillaService.getPlantilla(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PlantillaDE> createPlantilla(@RequestBody PlantillaTO to) {
        return ResponseEntity.status(201).body(plantillaService.createPlantilla(to));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlantillaDE> updatePlantilla(@PathVariable Long id, @RequestBody PlantillaTO to) {
        return ResponseEntity.ok(plantillaService.updatePlantilla(id, to));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarPlantilla(@PathVariable Long id) {
        plantillaService.desactivarPlantilla(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activarPlantilla(@PathVariable Long id) {
        plantillaService.activarPlantilla(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlantilla(@PathVariable Long id) {
        plantillaService.deletePlantilla(id);
        return ResponseEntity.noContent().build();
    }
}
