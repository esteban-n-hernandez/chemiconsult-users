package com.chemiconsult.service;

import com.chemiconsult.entity.*;
import com.chemiconsult.mapper.EstudiosMapper;
import com.chemiconsult.mapper.UserMapper;
import com.chemiconsult.repository.*;
import com.chemiconsult.to.AnalisisDetalleTO;
import com.chemiconsult.to.EstudioTO;
import com.chemiconsult.to.UserTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AnalisisService {

    private final AnalisisRepository analisisRepository;
    private final ClienteRepository clienteRepository;
    private final UserRepository userRepository;
    private final MatrizRepository matrizRepository;
    private final ResolucionDestinoRepository resolucionDestinoRepository;
    private final ParametroRepository parametroRepository;
    private final ResolucionDestinoParametroRepository resolucionDestinoParametroRepository;

    public List<AnalisisDE> getEstudios() {
        return analisisRepository.findAll();
    }

    public List<EstudioTO> getEstudiosTO() {
        return analisisRepository.findAll()
                .stream()
                .map(a -> {
                    ClienteDE cliente = buscarClientePorAnalisis(a);
                    return EstudiosMapper.mapEntityToEstudioTO(a, cliente);
                })
                .toList();
    }

    public List<EstudioTO> getEstudiosByID(Long userId) {
        UserDE user = UserMapper.mapUserToEntity(UserTO.builder().id(userId).build());
        return analisisRepository.findAllByUser(user)
                .stream()
                .map(EstudiosMapper::mapEntityToEstudioTO)
                .toList();
    }

    public Optional<AnalisisDE> getEstudio(Long id) {
        return this.analisisRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public AnalisisDetalleTO getEstudioDetalle(Long id) {
        AnalisisDE analisis = analisisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Muestra no encontrada con ID: " + id));

        ClienteDE cliente = buscarClientePorAnalisis(analisis);
        return EstudiosMapper.mapEntityToDetalleTO(analisis, cliente);
    }

    @Transactional
    public AnalisisDE createEstudio(EstudioTO estudio) {

        if (estudio.getNroProtocolo() == null || estudio.getNroProtocolo().isBlank()) {
            throw new RuntimeException("El número de protocolo es obligatorio");
        }
        if (analisisRepository.existsByNumeroProtocolo(estudio.getNroProtocolo())) {
            throw new RuntimeException("Ya existe una muestra con el protocolo: " + estudio.getNroProtocolo());
        }
        if (estudio.getParametrosIds() == null || estudio.getParametrosIds().isEmpty()) {
            throw new RuntimeException("Debe seleccionar al menos un parámetro a analizar");
        }

        UserDE user = userRepository.findById(estudio.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        MatrizDE matriz = matrizRepository.findById(estudio.getMatrizId())
                .orElseThrow(() -> new RuntimeException("Matriz no encontrada"));

        AnalisisDE analisis = EstudiosMapper.createEstudio(estudio, user, matriz);
        analisis = analisisRepository.save(analisis);

        List<Long> destinoIds = estudio.getResolucionDestinoIds() != null
                ? estudio.getResolucionDestinoIds() : List.of();

        List<AnalisisResolucionDestinoDE> resolucionesAplicadas = new ArrayList<>();
        for (Long destinoId : destinoIds) {
            ResolucionDestinoDE destino = resolucionDestinoRepository.findById(destinoId)
                    .orElseThrow(() -> new RuntimeException("Destino regulatorio no encontrado: " + destinoId));

            AnalisisResolucionDestinoDE ard = new AnalisisResolucionDestinoDE();
            ard.setAnalisis(analisis);
            ard.setResolucionDestino(destino);
            resolucionesAplicadas.add(ard);
        }
        analisis.setResolucionesAplicadas(resolucionesAplicadas);

        List<AnalisisParametroDE> parametros = new ArrayList<>();
        for (Long parametroId : estudio.getParametrosIds()) {
            ParametroDE parametro = parametroRepository.findById(parametroId)
                    .orElseThrow(() -> new RuntimeException("Parámetro no encontrado: " + parametroId));

            AnalisisParametroDE ap = new AnalisisParametroDE();
            ap.setAnalisis(analisis);
            ap.setParametro(parametro);

            List<ResolucionDestinoParametroDE> limitesEncontrados = destinoIds.isEmpty()
                    ? List.of()
                    : resolucionDestinoParametroRepository.findByDestinoIdsAndParametroId(destinoIds, parametroId);

            List<AnalisisParametroLimiteDE> limites = new ArrayList<>();
            for (ResolucionDestinoParametroDE origen : limitesEncontrados) {
                AnalisisParametroLimiteDE limite = new AnalisisParametroLimiteDE();
                limite.setAnalisisParametro(ap);
                limite.setLimiteOrigen(origen);
                limite.setLimiteMin(origen.getValorMinimo());
                limite.setLimiteMax(origen.getValorMaximo());
                limite.setLimiteTexto(origen.getLimiteTexto());
                limites.add(limite);

                if (ap.getMetodologiaUsada() == null) {
                    ap.setMetodologiaUsada(origen.getMetodologiaEstandar());
                }
            }
            ap.setLimites(limites);

            parametros.add(ap);
        }
        analisis.setParametros(parametros);

        return analisisRepository.save(analisis);
    }

    public AnalisisDE updateEstudio(Long id, AnalisisDE estudio) {
        AnalisisDE existing = analisisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudio no encontrado con ID: " + id));

        if (estudio.getEstado() != null) existing.setEstado(estudio.getEstado());
        existing.setObservaciones(estudio.getObservaciones());
        existing.setUpdateDate(LocalDate.now());
        if (estudio.getUser() != null) existing.setUser(estudio.getUser());
        if (estudio.getMatriz() != null) existing.setMatriz(estudio.getMatriz());

        return analisisRepository.save(existing);
    }

    public void deleteEstudio(Long id) {
        analisisRepository.deleteById(id);
    }

    private ClienteDE buscarClientePorAnalisis(AnalisisDE analisis) {
        if (analisis.getUser() == null) return null;
        return clienteRepository.findByUser_Id(analisis.getUser().getId()).orElse(null);
    }

    @Autowired
    public AnalisisService(AnalisisRepository analisisRepository,
                           ClienteRepository clienteRepository,
                           UserRepository userRepository,
                           MatrizRepository matrizRepository,
                           ResolucionDestinoRepository resolucionDestinoRepository,
                           ParametroRepository parametroRepository,
                           ResolucionDestinoParametroRepository resolucionDestinoParametroRepository) {
        this.analisisRepository = analisisRepository;
        this.clienteRepository = clienteRepository;
        this.userRepository = userRepository;
        this.matrizRepository = matrizRepository;
        this.resolucionDestinoRepository = resolucionDestinoRepository;
        this.parametroRepository = parametroRepository;
        this.resolucionDestinoParametroRepository = resolucionDestinoParametroRepository;
    }
}