package com.chemiconsult.mapper;

import com.chemiconsult.entity.EstudiosDE;
import com.chemiconsult.entity.UserDE;
import com.chemiconsult.to.EstudioTO;

import java.time.LocalDate;

public class EstudiosMapper {

    public static EstudiosDE createEstudio(EstudioTO estudio, UserDE user) {
        EstudiosDE entity = new EstudiosDE();
        entity.setTipo(estudio.getTipo());
        entity.setEstado(estudio.getEstado());
        entity.setArchivo(estudio.getArchivo());
        entity.setUser(user);
        entity.setCreatedDate(LocalDate.now());

        return entity;
    }


    //Create mapping method between EstudiosTO and EstudiosDE here
    public static EstudioTO toEstudioTO(EstudiosDE entity) {
        EstudioTO estudio = new EstudioTO();
        estudio.setTipo(entity.getTipo());
        estudio.setEstado(entity.getEstado());
        estudio.setArchivo(entity.getArchivo());
        // Note: User mapping should be handled separately
        return estudio;
    }

}
