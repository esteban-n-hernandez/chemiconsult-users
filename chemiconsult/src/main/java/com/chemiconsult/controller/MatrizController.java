package com.chemiconsult.controller;

import com.chemiconsult.entity.MatrizDE;
import com.chemiconsult.repository.MatrizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matrices")
public class MatrizController {

    private final MatrizRepository matrizRepository;

    @GetMapping
    public List<MatrizDE> getMatrices() {
        return matrizRepository.findAll();
    }

    @Autowired
    public MatrizController(MatrizRepository matrizRepository) {
        this.matrizRepository = matrizRepository;
    }
}