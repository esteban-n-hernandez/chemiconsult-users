package com.chemiconsult.service;

import com.chemiconsult.entity.MatrizDE;
import com.chemiconsult.entity.ParametroDE;
import com.chemiconsult.entity.ResolucionDE;
import com.chemiconsult.entity.ResolucionDestinoDE;
import com.chemiconsult.entity.ResolucionDestinoParametroDE;
import com.chemiconsult.repository.MatrizRepository;
import com.chemiconsult.repository.ParametroRepository;
import com.chemiconsult.repository.ResolucionDestinoParametroRepository;
import com.chemiconsult.repository.ResolucionDestinoRepository;
import com.chemiconsult.repository.ResolucionRepository;
import com.chemiconsult.to.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class ResolucionService {

    @Autowired
    private ResolucionDestinoRepository destinoRepository;

    @Autowired
    private ResolucionDestinoParametroRepository destinoParametroRepository;

    @Autowired
    private ParametroRepository parametroRepository;

    @Autowired
    private MatrizRepository matrizRepository;

    @Autowired
    private ResolucionRepository resolucionRepository;

    @Transactional(readOnly = true)
    public List<ResolucionDE> getAll() {
        return resolucionRepository.findAll();
    }

    @Transactional
    public ResolucionDE actualizar(Long id, String nombre, String descripcion) {
        ResolucionDE r = resolucionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Resolución no encontrada: " + id));
        r.setNombre(nombre);
        r.setDescripcion(descripcion);
        return resolucionRepository.save(r);
    }

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

    @Transactional(readOnly = true)
    public ResolucionTO obtenerDetalle(Long resolucionId) {
        ResolucionDE res = resolucionRepository.findById(resolucionId)
                .orElseThrow(() -> new EntityNotFoundException("Resolución no encontrada: " + resolucionId));

        ResolucionTO rTo = new ResolucionTO();
        rTo.setId(res.getId());
        rTo.setNombre(res.getNombre());
        rTo.setTieneDestino(res.getTieneDestino());
        rTo.setDestinos(res.getDestinos().stream()
                .map(this::mapDestinoConParametros)
                .collect(Collectors.toList()));
        return rTo;
    }

    @Transactional
    public void actualizarLimite(Long resolucionId, Long parametroId,
                                  String tipoLimite, Double valorMinimo, Double valorMaximo, String limiteTexto) {
        destinoParametroRepository.findByResolucionIdAndParametroId(resolucionId, parametroId)
                .forEach(rel -> {
                    rel.setTipoLimite(tipoLimite);
                    rel.setValorMinimo(valorMinimo);
                    rel.setValorMaximo(valorMaximo);
                    rel.setLimiteTexto(limiteTexto);
                    destinoParametroRepository.save(rel);
                });
    }

    @Transactional
    public void actualizarUnidad(Long resolucionId, Long parametroId, String unidad) {
        destinoParametroRepository.findByResolucionIdAndParametroId(resolucionId, parametroId)
                .forEach(rel -> { rel.setUnidad(unidad); destinoParametroRepository.save(rel); });
    }

    @Transactional
    public void agregarParametro(Long resolucionId, Long parametroId, String unidad) {
        ResolucionDE resolucion = resolucionRepository.findById(resolucionId)
                .orElseThrow(() -> new EntityNotFoundException("Resolución no encontrada: " + resolucionId));
        ParametroDE parametro = parametroRepository.findById(parametroId)
                .orElseThrow(() -> new EntityNotFoundException("Parámetro no encontrado: " + parametroId));

        String unidadFinal = (unidad != null && !unidad.isBlank()) ? unidad : parametro.getUnidad();

        List<ResolucionDestinoDE> destinos = resolucion.getDestinos();
        if (destinos == null || destinos.isEmpty()) {
            ResolucionDestinoDE destino = new ResolucionDestinoDE();
            destino.setResolucion(resolucion);
            destino.setNombre("Único");
            destinoRepository.save(destino);
            destinos = List.of(destino);
        }

        for (ResolucionDestinoDE destino : destinos) {
            destinoParametroRepository.findByDestinoIdAndParametroId(destino.getId(), parametroId)
                    .ifPresentOrElse(
                            rel -> {
                                rel.setActivo(true);
                                rel.setUnidad(unidadFinal);
                                destinoParametroRepository.save(rel);
                            },
                            () -> {
                                ResolucionDestinoParametroDE rel = new ResolucionDestinoParametroDE();
                                rel.setDestino(destino);
                                rel.setParametro(parametro);
                                rel.setUnidad(unidadFinal);
                                rel.setTipoLimite("NE");
                                rel.setActivo(true);
                                destinoParametroRepository.save(rel);
                            }
                    );
        }
    }

    @Transactional
    public void quitarParametro(Long resolucionId, Long parametroId) {
        destinoParametroRepository.findByResolucionIdAndParametroId(resolucionId, parametroId)
                .forEach(rel -> { rel.setActivo(false); destinoParametroRepository.save(rel); });
    }

    @Transactional
    public ResolucionDE crear(ResolucionCreateTO to) {
        MatrizDE matriz = matrizRepository.findById(to.getMatrizId())
                .orElseThrow(() -> new EntityNotFoundException("Matriz no encontrada: " + to.getMatrizId()));
        ResolucionDE res = new ResolucionDE();
        res.setNombre(to.getNombre());
        res.setDescripcion(to.getDescripcion());
        res.setMatriz(matriz);
        res.setTieneDestino(true);
        return resolucionRepository.save(res);
    }

    @Transactional
    public void eliminar(Long id) {
        resolucionRepository.deleteById(id);
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
        return destino.getParametrosConfigurados().stream()
                .filter(rel -> !Boolean.FALSE.equals(rel.getActivo()))
                .map(rel -> {
            ParametroNormaTO dto = new ParametroNormaTO();
            dto.setId(rel.getParametro().getId());
            dto.setNombre(rel.getParametro().getNombre());
            dto.setUnidad(rel.getUnidad());
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