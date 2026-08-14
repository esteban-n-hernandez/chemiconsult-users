package com.chemiconsult.service;

import com.chemiconsult.entity.EquipoDE;
import com.chemiconsult.repository.EquipoRepository;
import com.chemiconsult.to.EquipoTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class EquipoService {

    private final EquipoRepository repo;

    public EquipoService(EquipoRepository repo) {
        this.repo = repo;
    }

    public List<EquipoDE> getActivos() {
        return repo.findAllByActivoTrueOrderByNombreAsc();
    }

    public List<EquipoDE> getTodos() {
        return repo.findAllByOrderByNombreAsc();
    }

    public List<EquipoDE> findAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return repo.findAllById(ids);
    }

    public EquipoDE crear(EquipoTO to) {
        EquipoDE e = new EquipoDE();
        mapToEntity(to, e);
        e.setActivo(true);
        return repo.save(e);
    }

    public EquipoDE actualizar(Long id, EquipoTO to) {
        EquipoDE e = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        mapToEntity(to, e);
        if (to.getActivo() != null) e.setActivo(to.getActivo());
        return repo.save(e);
    }

    public void eliminar(Long id) {
        EquipoDE e = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        repo.delete(e);
    }

    private void mapToEntity(EquipoTO to, EquipoDE e) {
        if (to.getNombre() != null) e.setNombre(to.getNombre());
        e.setMarca(to.getMarca());
        e.setModelo(to.getModelo());
        e.setNroSerie(to.getNroSerie());
        e.setCertificacion(to.getCertificacion());
        e.setVencimiento(to.getVencimiento() != null && !to.getVencimiento().isBlank()
                ? LocalDate.parse(to.getVencimiento()) : null);
    }
}
