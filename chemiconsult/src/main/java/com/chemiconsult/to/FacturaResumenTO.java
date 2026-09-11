package com.chemiconsult.to;

import com.chemiconsult.enums.CondicionIVAEnum;
import com.chemiconsult.enums.EstadoPagoEnum;
import com.chemiconsult.enums.FacturaEstadoEnum;
import com.chemiconsult.enums.TipoComprobanteEnum;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FacturaResumenTO {
    private Long               id;
    private Long               numero;
    private Integer            puntoVenta;
    private TipoComprobanteEnum tipoComprobante;
    private LocalDate          fechaEmision;
    private String             clienteNombre;
    private String             clienteCuit;
    private CondicionIVAEnum   clienteCondicionIVA;
    private Double             subtotal;
    private Double             totalIva;
    private Double             total;
    private String             cae;
    private LocalDate          caeFechaVencimiento;
    private FacturaEstadoEnum  estado;
    private EstadoPagoEnum     estadoPago;
    private String             mensajeError;
    private LocalDateTime      createdAt;
    private List<ItemTO>       items;
    private Long               clienteId;
    private boolean            esExterna;
    private String             archivoNombre;

    @Data
    public static class ItemTO {
        private String  descripcion;
        private Double  cantidad;
        private Double  precioUnitario;
        private Double  alicuotaIva;
        private Double  subtotal;
        private Double  importeIva;
        private Double  total;
    }
}
