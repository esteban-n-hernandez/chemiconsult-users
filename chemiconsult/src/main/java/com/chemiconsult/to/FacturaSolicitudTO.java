package com.chemiconsult.to;

import com.chemiconsult.enums.CondicionIVAEnum;
import com.chemiconsult.enums.TipoComprobanteEnum;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FacturaSolicitudTO {
    private Long            clienteId;
    private String          clienteNombre;
    private String          clienteCuit;
    private String          clienteDireccion;
    private CondicionIVAEnum clienteCondicionIVA;
    private TipoComprobanteEnum tipoComprobante;
    private Integer         puntoVenta;        // default 1
    private LocalDate       fechaEmision;
    private Long            presupuestoId;
    private List<FacturaItemTO> items;
}
