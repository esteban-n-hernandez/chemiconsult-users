package com.chemiconsult.service;

import com.chemiconsult.entity.CategoriaDocumentoDE;
import com.chemiconsult.repository.CategoriaDocumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaDocumentoService {

    private final CategoriaDocumentoRepository repo;

    @Autowired
    public CategoriaDocumentoService(CategoriaDocumentoRepository repo) {
        this.repo = repo;
    }

    public List<CategoriaDocumentoDE> getAll() {
        return repo.findAllByOrderByNombreAsc();
    }

    public CategoriaDocumentoDE create(String nombre) {
        String trimmed = nombre.trim();
        return repo.findByNombreIgnoreCase(trimmed)
                .orElseGet(() -> {
                    CategoriaDocumentoDE cat = new CategoriaDocumentoDE();
                    cat.setNombre(trimmed);
                    return repo.save(cat);
                });
    }

    public CategoriaDocumentoDE actualizar(Long id, String nombre) {
        CategoriaDocumentoDE cat = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        cat.setNombre(nombre.trim());
        return repo.save(cat);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada");
        }
        repo.deleteById(id);
    }
}
