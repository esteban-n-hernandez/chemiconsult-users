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


    public static EstudioTO mapEntityToEstudioTO(AnalisisDE entity) {
        return EstudioTO.builder()
                .archivo(entity.getArchivo())
                .archivoUrl(entity.getArchivoUrl())
                .estado(entity.getEstado())
                .tipo(entity.getTipo())
                .build();
    }

}
