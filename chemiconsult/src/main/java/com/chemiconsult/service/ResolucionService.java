package com.chemiconsult.service;

import com.chemiconsult.entity.MatrizDE;
import com.chemiconsult.entity.ResolucionDestinoDE;
import com.chemiconsult.repository.MatrizRepository;
import com.chemiconsult.repository.ResolucionDestinoRepository;
import com.chemiconsult.to.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResolucionService {

    @Autowired
    private ResolucionDestinoRepository destinoRepository;

    @Autowired
    private MatrizRepository matrizRepository;

    @Transactional(readOnly = true)
    public List<ResolucionDestinoTO> listarTodosLosDestinos() {
        return destinoRepository.findAll().stream()
                .map(this::mapDestinoSimple)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParametroNormaTO> obtenerParametrosPorDestino(Long destinoId) {
        ResolucionDestinoDE destino = destinoRepository.findById(destinoId)
                .orElseThrow(() -> new RuntimeException("Destino regulatorio no encontrado"));

        return mapParametros(destino);
    }

    // ── Endpoint nuevo: árbol completo Matriz → Resoluciones → Destinos → Parámetros ──
    @Transactional(readOnly = true)
    public MatrizResolucionesTO obtenerArbolPorMatriz(Long matrizId) {
        MatrizDE matriz = matrizRepository.findById(matrizId)
                .orElseThrow(() -> new RuntimeException("Matriz no encontrada"));

        MatrizResolucionesTO arbol = new MatrizResolucionesTO();
        arbol.setMatrizId(matriz.getId());
        arbol.setMatrizNombre(matriz.getNombre());

        List<ResolucionTO> resoluciones = matriz.getResoluciones().stream().map(res -> {
            ResolucionTO rTo = new ResolucionTO();
            rTo.setId(res.getId());
            rTo.setNombre(res.getNombre());
            rTo.setTieneDestino(res.getTieneDestino());

            List<ResolucionDestinoTO> destinos = res.getDestinos().stream()
                    .map(this::mapDestinoConParametros)
                    .collect(Collectors.toList());

            rTo.setDestinos(destinos);
            return rTo;
        }).collect(Collectors.toList());

        arbol.setResoluciones(resoluciones);
        return arbol;
    }

    // ── Helpers ──
    private ResolucionDestinoTO mapDestinoSimple(ResolucionDestinoDE destino) {
        ResolucionDestinoTO dto = new ResolucionDestinoTO();
        dto.setId(destino.getId());
        dto.setNombre(destino.getResolucion().getNombre() + " - " + destino.getNombre());
        return dto;
    }

    private ResolucionDestinoTO mapDestinoConParametros(ResolucionDestinoDE destino) {
        ResolucionDestinoTO dto = new ResolucionDestinoTO();
        dto.setId(destino.getId());
        dto.setNombre(destino.getNombre());
        dto.setParametros(mapParametros(destino));
        return dto;
    }

    private List<ParametroNormaTO> mapParametros(ResolucionDestinoDE destino) {
        return destino.getParametrosConfigurados().stream().map(rel -> {
            ParametroNormaTO dto = new ParametroNormaTO();
            dto.setId(rel.getParametro().getId());
            dto.setNombre(rel.getParametro().getNombre());
            dto.setUnidad(rel.getParametro().getUnidad());
            dto.setTipoLimite(rel.getTipoLimite());
            dto.setValorMinimo(rel.getValorMinimo());
            dto.setValorMaximo(rel.getValorMaximo());
            dto.setLimiteTexto(rel.getLimiteTexto());

            MetodologiaSimpleTO mDto = new MetodologiaSimpleTO();
            if (rel.getMetodologiaEstandar() != null) {
                mDto.setNombre(rel.getMetodologiaEstandar().getNombre());
                mDto.setDescripcion(rel.getMetodologiaEstandar().getDescripcion());
            } else {
                mDto.setNombre("Sin método");
            }
            dto.setMetodologia(mDto);

            return dto;
        }).collect(Collectors.toList());
    }
}