package com.chemiconsult.service;

import com.chemiconsult.entity.ParametroDE;
import com.chemiconsult.entity.PlantillaDE;
import com.chemiconsult.repository.ParametroRepository;
import com.chemiconsult.repository.PlantillaRepository;
import com.chemiconsult.to.PlantillaTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PlantillaService {

    @Autowired private PlantillaRepository plantillaRepository;
    @Autowired private ParametroRepository parametroRepository;

    @Transactional(readOnly = true)
    public List<PlantillaDE> getPlantillas() {
        return plantillaRepository.findByActivoTrueWithParametros();
    }

    @Transactional(readOnly = true)
    public List<PlantillaDE> getPlantillasTodas() {
        return plantillaRepository.findAllWithParametros();
    }

    public Optional<PlantillaDE> getPlantilla(Long id) {
        return plantillaRepository.findById(id);
    }

    public PlantillaDE createPlantilla(PlantillaTO to) {
        PlantillaDE plantilla = buildFromTO(new PlantillaDE(), to);
        plantilla.setActivo(true);
        plantilla.setCreatedDate(LocalDate.now());
        plantilla.setUpdateDate(LocalDate.now());
        return plantillaRepository.save(plantilla);
    }

    public PlantillaDE updatePlantilla(Long id, PlantillaTO to) {
        PlantillaDE existing = plantillaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada: " + id));
        buildFromTO(existing, to);
        existing.setUpdateDate(LocalDate.now());
        return plantillaRepository.save(existing);
    }

    public void desactivarPlantilla(Long id) {
        PlantillaDE plantilla = plantillaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada: " + id));
        plantilla.setActivo(false);
        plantilla.setUpdateDate(LocalDate.now());
        plantillaRepository.save(plantilla);
    }

    public void activarPlantilla(Long id) {
        PlantillaDE plantilla = plantillaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada: " + id));
        plantilla.setActivo(true);
        plantilla.setUpdateDate(LocalDate.now());
        plantillaRepository.save(plantilla);
    }

    public void deletePlantilla(Long id) {
        if (!plantillaRepository.existsById(id)) {
            throw new RuntimeException("Plantilla no encontrada: " + id);
        }
        plantillaRepository.deleteById(id);
    }

    private PlantillaDE buildFromTO(PlantillaDE plantilla, PlantillaTO to) {
        List<ParametroDE> parametros = parametroRepository.findAllById(
                to.getParametroIds() != null ? to.getParametroIds() : List.of());
        plantilla.setNombre(to.getNombre());
        plantilla.setDescripcion(to.getDescripcion());
        plantilla.setParametros(parametros);
        return plantilla;
    }
}
