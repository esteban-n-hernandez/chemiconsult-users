package com.chemiconsult.service;

import com.chemiconsult.entity.CategoriaDocumentoDE;
import com.chemiconsult.repository.CategoriaDocumentoRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoriaDocumentoSeeder implements ApplicationRunner {

    private final CategoriaDocumentoRepository repo;

    public CategoriaDocumentoSeeder(CategoriaDocumentoRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repo.count() > 0) return;
        List.of("Cert. Calibración", "Cert. Acreditación", "Cert. Calidad",
                "Manual de equipo", "Normativa", "Otro")
                .forEach(nombre -> {
                    CategoriaDocumentoDE cat = new CategoriaDocumentoDE();
                    cat.setNombre(nombre);
                    repo.save(cat);
                });
    }
}
