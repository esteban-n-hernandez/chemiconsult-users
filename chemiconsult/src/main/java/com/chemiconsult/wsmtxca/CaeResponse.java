package com.chemiconsult.wsmtxca;

import java.time.LocalDate;
import java.util.List;

/**
 * Respuesta de ARCA para una solicitud de CAE.
 * resultado: "A" = Autorizado, "R" = Rechazado.
 */
public record CaeResponse(
        boolean    autorizado,
        String     cae,
        LocalDate  caeFechaVencimiento,
        String     resultado,
        List<String> observaciones
) {}
