package com.chemiconsult.service;

import com.chemiconsult.entity.EstudiosDE;
import com.chemiconsult.repository.EstudiosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EstudiosService {


    @Autowired
    EstudiosRepository estudiosRepository;

    public List<EstudiosDE> getEstudios() {
        return estudiosRepository.findAll();
    }

    public EstudiosDE createEstudio(EstudiosDE estudio) {
        return estudiosRepository.save(estudio);
    }

    public EstudiosDE updateEstudio(Long id, EstudiosDE estudio) {
        Optional<EstudiosDE> optional = estudiosRepository.findById(id);
        if (optional.isPresent()) {
            EstudiosDE existing = optional.get();
            existing.setDate(estudio.getDate());
            existing.setArchivo(estudio.getArchivo());
            existing.setUser(estudio.getUser());
            return estudiosRepository.save(existing);
        }
        throw new RuntimeException("Estudio no encontrado con ID: " + id);
    }

    public void deleteEstudio(Long id) {
        estudiosRepository.deleteById(id);
    }
}
