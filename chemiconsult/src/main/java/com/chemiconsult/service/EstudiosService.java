package com.chemiconsult.service;

import com.chemiconsult.entity.EstudiosDE;
import com.chemiconsult.entity.UserDE;
import com.chemiconsult.mapper.EstudiosMapper;
import com.chemiconsult.repository.EstudiosRepository;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.to.EstudioTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EstudiosService {


    @Autowired
    EstudiosRepository estudiosRepository;
    @Autowired
    private UserRepository userRepository;

    public List<EstudiosDE> getEstudios() {
        return estudiosRepository.findAll();
    }

    public Optional<EstudiosDE> getEstudio(Long id) {
        return this.estudiosRepository.findById(id);
    }

    public EstudiosDE createEstudio(EstudioTO estudio) {

        UserDE user = userRepository.findById(Math.toIntExact(estudio.getUserId()))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        EstudiosDE estudioDE = EstudiosMapper.createEstudio(estudio,user);

        return estudiosRepository.save(estudioDE);
    }

    public EstudiosDE updateEstudio(Long id, EstudiosDE estudio) {
        Optional<EstudiosDE> optional = estudiosRepository.findById(id);
        if (optional.isPresent()) {
            EstudiosDE existing = optional.get();
            existing.setCreatedDate(estudio.getCreatedDate());
            existing.setArchivo(estudio.getArchivo());
            existing.setUser(estudio.getUser());
            return estudiosRepository.save(existing);
        }
        throw new RuntimeException("Estudio no encontrado con ID: " + id);
    }

    public void deleteEstudio(Long id) {
        estudiosRepository.deleteById(id);
    }

}
