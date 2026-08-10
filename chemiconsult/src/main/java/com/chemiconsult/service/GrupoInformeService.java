package com.chemiconsult.service;

import com.chemiconsult.entity.GrupoInformeDE;
import com.chemiconsult.repository.GrupoInformeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GrupoInformeService {

    @Autowired
    private GrupoInformeRepository repo;

    @Transactional(readOnly = true)
    public List<GrupoInformeDE> getAll() {
        return repo.findAllByOrderByOrdenAsc();
    }

    @Transactional
    public GrupoInformeDE crear(String codigo, String label, Integer orden) {
        GrupoInformeDE g = new GrupoInformeDE();
        g.setCodigo(codigo.trim().toUpperCase().replace(" ", "_"));
        g.setLabel(label.trim());
        g.setOrden(orden != null ? orden : 0);
        return repo.save(g);
    }

    @Transactional
    public GrupoInformeDE actualizar(Long id, String codigo, String label, Integer orden) {
        GrupoInformeDE g = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado: " + id));
        if (codigo != null && !codigo.isBlank())
            g.setCodigo(codigo.trim().toUpperCase().replace(" ", "_"));
        if (label != null && !label.isBlank())
            g.setLabel(label.trim());
        if (orden != null)
            g.setOrden(orden);
        return repo.save(g);
    }

    @Transactional
    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<String, String> buildLabelMap() {
        return repo.findAllByOrderByOrdenAsc().stream()
                .collect(Collectors.toMap(GrupoInformeDE::getCodigo, GrupoInformeDE::getLabel,
                        (a, b) -> a));
    }
}
