package com.chemiconsult.wsmtxca;

import com.chemiconsult.enums.TipoComprobanteEnum;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación MOCK del web service WSMTXCA de ARCA.
 * Genera CAEs sintéticos sin conectarse al servicio real.
 * Reemplazar con una implementación SOAP cuando se disponga de certificado ARCA.
 */
@Log4j2
@Component
public class WsmtxcaMockPort implements WsmtxcaPort {

    private final Map<String, Long> contadores = new ConcurrentHashMap<>();

    @Override
    public long getUltimoComprobanteAutorizado(int puntoVenta, TipoComprobanteEnum tipo) {
        return contadores.getOrDefault(clave(puntoVenta, tipo), 0L);
    }

    @Override
    public CaeResponse solicitarCAE(CaeSolicitud solicitud) {
        String clave = clave(solicitud.puntoVenta(), solicitud.tipoComprobante());
        contadores.merge(clave, solicitud.cbteHasta(), Math::max);

        // CAE ficticio de 14 dígitos basado en timestamp
        long base = (System.currentTimeMillis() % 10_000_000_000L) + 20_000_000_000L;
        String cae = String.format("%014d", base);
        LocalDate vto = LocalDate.now().plusDays(10);

        log.info("[MOCK WSMTXCA] CAE={} Vto={} PtoVta={} Tipo={} Cbte={}",
                cae, vto, String.format("%04d", solicitud.puntoVenta()),
                solicitud.tipoComprobante(), solicitud.cbteHasta());

        return new CaeResponse(
                true,
                cae,
                vto,
                "A",
                List.of("Modo MOCK — sin conexión real a ARCA/AFIP")
        );
    }

    private String clave(int puntoVenta, TipoComprobanteEnum tipo) {
        return puntoVenta + "-" + tipo.name();
    }
}
