package com.chemiconsult.service;

import org.springframework.stereotype.Service;
import com.chemiconsult.entity.MetodologiaDE;
import com.chemiconsult.entity.MatrizDE;
import com.chemiconsult.entity.ParametroDE;
import com.chemiconsult.entity.ParametroMetodologiaDE;
import com.chemiconsult.mapper.ParametroMapper;
import com.chemiconsult.repository.MatrizRepository;
import com.chemiconsult.repository.MetodologiaRepository;
import com.chemiconsult.repository.ParametroMetodologiaRepository;
import com.chemiconsult.repository.ParametroRepository;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.to.ParametroTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ParametroService {

    @Autowired
    private ParametroRepository parametroRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParametroMetodologiaRepository pmRepository;

    @Autowired
    private MetodologiaRepository metodologiaRepository;

    @Autowired
    private MatrizRepository matrizRepository;

    public List<ParametroDE> getParametros() {
        return parametroRepository.findByActivoTrue();
    }

    public List<ParametroDE> getParametrosTodos() {
        return parametroRepository.findAll();
    }

    public Optional<ParametroDE> getParametro(Long id) {
        return parametroRepository.findById(id);
    }

    public ParametroDE createParametro(ParametroTO to) {
        if (parametroRepository.existsByNombreIgnoreCase(to.getNombre())) {
            throw new RuntimeException("Ya existe un parámetro con el nombre: " + to.getNombre());
        }
        ParametroDE parametro = ParametroMapper.createParametro(to);
        if (to.getResponsableId() != null) {
            userRepository.findById(to.getResponsableId()).ifPresent(parametro::setResponsable);
        }
        return parametroRepository.save(parametro);
    }

    public ParametroDE updateParametro(Long id, ParametroTO to) {
        ParametroDE existing = parametroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parámetro no encontrado con ID: " + id));
        ParametroMapper.updateParametro(existing, to);
        if (to.getResponsableId() != null) {
            userRepository.findById(to.getResponsableId()).ifPresent(existing::setResponsable);
        } else {
            existing.setResponsable(null);
        }
        return parametroRepository.save(existing);
    }

    public void desactivarParametro(Long id) {
        Optional<ParametroDE> optional = parametroRepository.findById(id);
        if (optional.isPresent()) {
            ParametroDE parametro = optional.get();
            parametro.setActivo(false);
            parametro.setUpdateDate(LocalDate.now());
            parametroRepository.save(parametro);
            return;
        }
        throw new RuntimeException("Parámetro no encontrado con ID: " + id);
    }

    public void deleteParametro(Long id) {
        if (!parametroRepository.existsById(id)) {
            throw new RuntimeException("Parámetro no encontrado con ID: " + id);
        }
        parametroRepository.deleteById(id);
    }

    // ── Metodologías asociadas al parámetro ──────────────────────────────────

    public List<ParametroMetodologiaDE> getMetodologias(Long parametroId, Long matrizId) {
        if (!parametroRepository.existsById(parametroId)) {
            throw new RuntimeException("Parámetro no encontrado con ID: " + parametroId);
        }
        if (matrizId != null) {
            return pmRepository.findByParametroIdAndMatriz(parametroId, matrizId);
        }
        return pmRepository.findByParametroId(parametroId);
    }

    public ParametroMetodologiaDE addMetodologia(Long parametroId, Long metodologiaId, Long matrizId, String tipoAnalisis) {
        ParametroDE parametro = parametroRepository.findById(parametroId)
                .orElseThrow(() -> new RuntimeException("Parámetro no encontrado: " + parametroId));
        MetodologiaDE metodologia = metodologiaRepository.findById(metodologiaId)
                .orElseThrow(() -> new RuntimeException("Metodología no encontrada: " + metodologiaId));

        boolean duplicado = (matrizId != null)
                ? pmRepository.existsByParametroIdAndMetodologiaIdAndMatrizId(parametroId, metodologiaId, matrizId)
                : pmRepository.existsByParametroIdAndMetodologiaIdAndMatrizIsNull(parametroId, metodologiaId);
        if (duplicado) {
            throw new RuntimeException("Ya existe esta metodología para la combinación indicada.");
        }

        ParametroMetodologiaDE pm = new ParametroMetodologiaDE();
        pm.setParametro(parametro);
        pm.setMetodologia(metodologia);
        pm.setTipoAnalisis(tipoAnalisis);
        if (matrizId != null) {
            MatrizDE matriz = matrizRepository.findById(matrizId)
                    .orElseThrow(() -> new RuntimeException("Matriz no encontrada: " + matrizId));
            pm.setMatriz(matriz);
        }
        return pmRepository.save(pm);
    }

    public ParametroMetodologiaDE updateMetodologiaTipo(Long parametroId, Long pmId, String tipoAnalisis) {
        ParametroMetodologiaDE pm = pmRepository.findById(pmId)
                .orElseThrow(() -> new RuntimeException("Asociación no encontrada: " + pmId));
        if (!pmRepository.existsByIdAndParametroId(pmId, parametroId)) {
            throw new RuntimeException("La asociación no pertenece al parámetro indicado.");
        }
        pm.setTipoAnalisis(tipoAnalisis != null && !tipoAnalisis.isBlank() ? tipoAnalisis : null);
        return pmRepository.save(pm);
    }

    public void removeMetodologia(Long parametroId, Long pmId) {
        if (!pmRepository.existsById(pmId)) {
            throw new RuntimeException("Asociación no encontrada: " + pmId);
        }
        if (!pmRepository.existsByIdAndParametroId(pmId, parametroId)) {
            throw new RuntimeException("La asociación no pertenece al parámetro indicado.");
        }
        pmRepository.deleteById(pmId);
    }
}
