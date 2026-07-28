package com.chemiconsult.service;

import com.chemiconsult.entity.MatrizDE;
import com.chemiconsult.entity.TipoMuestraDE;
import com.chemiconsult.mapper.TipoMuestraMapper;
import com.chemiconsult.repository.MatrizRepository;
import com.chemiconsult.repository.TipoMuestraRepository;
import com.chemiconsult.to.TipoMuestraTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TipoMuestraService {

    @Autowired
    private TipoMuestraRepository tipoMuestraRepository;

    @Autowired
    private MatrizRepository matrizRepository;

    public List<TipoMuestraDE> getTiposMuestra() {
        return tipoMuestraRepository.findByActivoTrue();
    }

    public List<TipoMuestraDE> getTiposMuestraTodos() {
        return tipoMuestraRepository.findAll();
    }

    public Optional<TipoMuestraDE> getTipoMuestra(Long id) {
        return tipoMuestraRepository.findById(id);
    }

    public TipoMuestraDE createTipoMuestra(TipoMuestraTO to) {
        if (tipoMuestraRepository.existsByNombreIgnoreCase(to.getNombre())) {
            throw new RuntimeException("Ya existe un tipo de muestra con el nombre: " + to.getNombre());
        }

        MatrizDE matriz = matrizRepository.findById(to.getMatrizId())
                .orElseThrow(() -> new RuntimeException("Matriz no encontrada con ID: " + to.getMatrizId()));

        TipoMuestraDE tipoMuestra = TipoMuestraMapper.createTipoMuestra(to, matriz);
        return tipoMuestraRepository.save(tipoMuestra);
    }

    public TipoMuestraDE updateTipoMuestra(Long id, TipoMuestraTO to) {
        TipoMuestraDE existing = tipoMuestraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de muestra no encontrado con ID: " + id));

        MatrizDE matriz = matrizRepository.findById(to.getMatrizId())
                .orElseThrow(() -> new RuntimeException("Matriz no encontrada con ID: " + to.getMatrizId()));

        TipoMuestraDE actualizado = TipoMuestraMapper.updateTipoMuestra(existing, to, matriz);
        return tipoMuestraRepository.save(actualizado);
    }

    public void desactivarTipoMuestra(Long id) {
        TipoMuestraDE tipoMuestra = tipoMuestraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de muestra no encontrado con ID: " + id));
        tipoMuestra.setActivo(false);
        tipoMuestra.setUpdateDate(LocalDate.now());
        tipoMuestraRepository.save(tipoMuestra);
    }

    public void deleteTipoMuestra(Long id) {
        if (!tipoMuestraRepository.existsById(id)) {
            throw new RuntimeException("Tipo de muestra no encontrado con ID: " + id);
        }
        tipoMuestraRepository.deleteById(id);
    }
}