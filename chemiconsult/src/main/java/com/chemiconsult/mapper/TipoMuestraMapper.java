package com.chemiconsult.mapper;

import com.chemiconsult.entity.MatrizDE;
import com.chemiconsult.entity.TipoMuestraDE;
import com.chemiconsult.to.TipoMuestraTO;

import java.time.LocalDate;

public class TipoMuestraMapper {

    public static TipoMuestraDE createTipoMuestra(TipoMuestraTO to, MatrizDE matriz) {
        TipoMuestraDE tipoMuestra = new TipoMuestraDE();
        tipoMuestra.setNombre(to.getNombre());
        tipoMuestra.setDescripcion(to.getDescripcion());
        tipoMuestra.setMatriz(matriz);
        tipoMuestra.setActivo(true);
        tipoMuestra.setCreatedDate(LocalDate.now());
        tipoMuestra.setUpdateDate(LocalDate.now());
        return tipoMuestra;
    }

    public static TipoMuestraDE updateTipoMuestra(TipoMuestraDE existing,
                                                  TipoMuestraTO to,
                                                  MatrizDE matriz) {
        existing.setNombre(to.getNombre());
        existing.setDescripcion(to.getDescripcion());
        existing.setMatriz(matriz);
        existing.setUpdateDate(LocalDate.now());
        return existing;
    }
}