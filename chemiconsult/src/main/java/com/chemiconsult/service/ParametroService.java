package com.chemiconsult.service;

import org.springframework.stereotype.Service;
import com.chemiconsult.entity.ParametroDE;
import com.chemiconsult.mapper.ParametroMapper;
import com.chemiconsult.repository.ParametroRepository;
import com.chemiconsult.to.ParametroTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ParametroService {

    @Autowired
    private ParametroRepository parametroRepository;

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
        return parametroRepository.save(parametro);
    }

    public ParametroDE updateParametro(Long id, ParametroTO to) {
        Optional<ParametroDE> optional = parametroRepository.findById(id);
        if (optional.isPresent()) {
            ParametroDE actualizado = ParametroMapper.updateParametro(optional.get(), to);
            return parametroRepository.save(actualizado);
        }
        throw new RuntimeException("Parámetro no encontrado con ID: " + id);
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
}
