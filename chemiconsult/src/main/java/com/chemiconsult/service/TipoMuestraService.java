package com.chemiconsult.service;

import com.chemiconsult.entity.ParametroDE;
import com.chemiconsult.entity.TipoMuestraDE;
import com.chemiconsult.mapper.TipoMuestraMapper;
import com.chemiconsult.repository.ParametroRepository;
import com.chemiconsult.repository.TipoMuestraRepository;
import com.chemiconsult.to.TipoMuestraTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TipoMuestraService {

    @Autowired
    private TipoMuestraRepository tipoMuestraRepository;

    @Autowired
    private ParametroRepository parametroRepository;

    // ── Solo activos ──
    public List<TipoMuestraDE> getTiposMuestra() {
        return tipoMuestraRepository.findByActivoTrue();
    }

    // ── Todos incluyendo inactivos ──
    public List<TipoMuestraDE> getTiposMuestraTodos() {
        return tipoMuestraRepository.findAll();
    }

    // ── Por ID ──
    public Optional<TipoMuestraDE> getTipoMuestra(Long id) {
        return tipoMuestraRepository.findById(id);
    }

    // ── Crear ──
    public TipoMuestraDE createTipoMuestra(TipoMuestraTO to) {
        if (tipoMuestraRepository.existsByNombreIgnoreCase(to.getNombre())) {
            throw new RuntimeException("Ya existe un tipo de muestra con el nombre: " + to.getNombre());
        }

        List<ParametroDE> parametros = resolverParametros(to.getParametroIds());
        TipoMuestraDE tipoMuestra = TipoMuestraMapper.createTipoMuestra(to, parametros);

        return tipoMuestraRepository.save(tipoMuestra);
    }

    // ── Actualizar ──
    public TipoMuestraDE updateTipoMuestra(Long id, TipoMuestraTO to) {
        Optional<TipoMuestraDE> optional = tipoMuestraRepository.findById(id);
        if (optional.isPresent()) {
            List<ParametroDE> parametros = resolverParametros(to.getParametroIds());
            TipoMuestraDE actualizado = TipoMuestraMapper.updateTipoMuestra(optional.get(), to, parametros);
            return tipoMuestraRepository.save(actualizado);
        }
        throw new RuntimeException("Tipo de muestra no encontrado con ID: " + id);
    }

    // ── Desactivar (baja lógica) ──
    public void desactivarTipoMuestra(Long id) {
        Optional<TipoMuestraDE> optional = tipoMuestraRepository.findById(id);
        if (optional.isPresent()) {
            TipoMuestraDE tipoMuestra = optional.get();
            tipoMuestra.setActivo(false);
            tipoMuestra.setUpdateDate(LocalDate.now());
            tipoMuestraRepository.save(tipoMuestra);
            return;
        }
        throw new RuntimeException("Tipo de muestra no encontrado con ID: " + id);
    }

    // ── Eliminar físico ──
    public void deleteTipoMuestra(Long id) {
        if (!tipoMuestraRepository.existsById(id)) {
            throw new RuntimeException("Tipo de muestra no encontrado con ID: " + id);
        }
        tipoMuestraRepository.deleteById(id);
    }

    // ── Helper: resuelve los IDs de parámetros recibidos ──
    private List<ParametroDE> resolverParametros(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<ParametroDE> parametros = parametroRepository.findAllById(ids);
        if (parametros.size() != ids.size()) {
            throw new RuntimeException("Uno o más parámetros no fueron encontrados");
        }
        return parametros;
    }
}
