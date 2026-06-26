package com.chemiconsult.mapper;

import com.chemiconsult.entity.ParametroDE;
import com.chemiconsult.entity.TipoMuestraDE;
import com.chemiconsult.to.TipoMuestraTO;

import java.time.LocalDate;
import java.util.List;

public class TipoMuestraMapper {

    public static TipoMuestraDE createTipoMuestra(TipoMuestraTO to, List<ParametroDE> parametros) {
        TipoMuestraDE tipoMuestra = new TipoMuestraDE();
        tipoMuestra.setNombre(to.getNombre());
        tipoMuestra.setDescripcion(to.getDescripcion());
        tipoMuestra.setParametrosPorDefecto(parametros);
        tipoMuestra.setActivo(true);
        tipoMuestra.setCreatedDate(LocalDate.now());
     //   tipoMuestra.setUpdateDate(LocalDate.now());
        return tipoMuestra;
    }

    public static TipoMuestraDE updateTipoMuestra(TipoMuestraDE existing,
                                                  TipoMuestraTO to,
                                                  List<ParametroDE> parametros) {
        existing.setNombre(to.getNombre());
        existing.setDescripcion(to.getDescripcion());
        existing.setParametrosPorDefecto(parametros);
      //  existing.setUpdateDate(LocalDate.now());
        return existing;
    }
}
