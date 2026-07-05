package com.chemiconsult.service;

import com.chemiconsult.entity.AnalisisDE;
import com.chemiconsult.entity.ClienteDE;
import com.chemiconsult.entity.UserDE;
import com.chemiconsult.mapper.EstudiosMapper;
import com.chemiconsult.mapper.UserMapper;
import com.chemiconsult.repository.AnalisisRepository;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.to.EstudioTO;
import com.chemiconsult.to.UserTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnalisisService {

    AnalisisRepository analisisRepository;

    ClienteService clienteService;

    UserRepository userRepository;

    public List<AnalisisDE> getEstudios() {
        return analisisRepository.findAll();
    }

    public List<EstudioTO> getEstudiosTO() {
        return analisisRepository.findAll()
                .stream()
                .map(a -> {
                    Long userId = a.getUser() != null ? a.getUser().getId() : null;
                    ClienteDE cliente = null;
                    if (userId != null) {
                        cliente = clienteService.getCliente(userId);
                    }
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

    public AnalisisDE createEstudio(EstudioTO estudio) {

        UserDE user = userRepository.findById(Math.toIntExact(estudio.getUserId()))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        AnalisisDE estudioDE = EstudiosMapper.createEstudio(estudio, user);

        return analisisRepository.save(estudioDE);
    }

    public AnalisisDE updateEstudio(Long id, AnalisisDE estudio) {
        Optional<AnalisisDE> optional = analisisRepository.findById(id);
        if (optional.isPresent()) {
            AnalisisDE existing = optional.get();
            existing.setCreatedDate(estudio.getCreatedDate());
            existing.setArchivo(estudio.getArchivo());
            existing.setUser(estudio.getUser());
            return analisisRepository.save(existing);
        }
        throw new RuntimeException("Estudio no encontrado con ID: " + id);
    }

    public void deleteEstudio(Long id) {
        analisisRepository.deleteById(id);
    }


    @Autowired
    public AnalisisService(AnalisisRepository analisisRepository,
                           ClienteService clienteService, UserRepository userRepository) {
        this.analisisRepository = analisisRepository;
        this.clienteService = clienteService;
        this.userRepository = userRepository;
    }
}
