package com.chemiconsult.service;

import com.chemiconsult.entity.MetodologiaDE;
import com.chemiconsult.repository.MetodologiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetodologiaService {

    @Autowired
    private MetodologiaRepository repository;

    public List<MetodologiaDE> obtenerActivas() {
        return repository.findByActivoTrue();
    }

    public MetodologiaDE guardar(MetodologiaDE metodologia) {
        // Nos aseguramos de que siempre nazca activa por defecto
        metodologia.setActivo(true);
        return repository.save(metodologia);
    }

    public void bajaLogica(Long id) {
        // Si existe, le cambiamos el estado a activo = false
        repository.findById(id).ifPresent(met -> {
            met.setActivo(false);
            repository.save(met);
        });
    }
}