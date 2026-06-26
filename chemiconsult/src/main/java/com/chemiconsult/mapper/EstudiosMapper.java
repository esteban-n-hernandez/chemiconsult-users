package com.chemiconsult.mapper;

import com.chemiconsult.entity.AnalisisDE;
import com.chemiconsult.entity.UserDE;
import com.chemiconsult.to.EstudioTO;

import java.time.LocalDate;

public class EstudiosMapper {

    public static AnalisisDE createEstudio(EstudioTO estudio, UserDE user) {
        AnalisisDE entity = new AnalisisDE();
        entity.setTipo(estudio.getTipo());
        entity.setEstado(estudio.getEstado());
        entity.setArchivo(estudio.getArchivo());
        entity.setUser(user);
        entity.setCreatedDate(LocalDate.now());

        return entity;
    }


    //Create mapping method between EstudiosTO and EstudiosDE here
    public static EstudioTO toEstudioTO(AnalisisDE entity) {
        EstudioTO estudio = new EstudioTO();
        estudio.setTipo(entity.getTipo());
        estudio.setEstado(entity.getEstado());
        estudio.setArchivo(entity.getArchivo());
        // Note: User mapping should be handled separately
        return estudio;
    }

}
