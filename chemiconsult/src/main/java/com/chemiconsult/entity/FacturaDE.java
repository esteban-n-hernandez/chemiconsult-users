package com.chemiconsult.entity;

import com.chemiconsult.enums.CondicionIVAEnum;
import com.chemiconsult.enums.EstadoPagoEnum;
import com.chemiconsult.enums.FacturaEstadoEnum;
import com.chemiconsult.enums.TipoComprobanteEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "FACTURAS")
@Data
public class FacturaDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false)
    private Long numero;

    @Column(name = "punto_venta", nullable = false)
    private Integer puntoVenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", nullable = false)
    private TipoComprobanteEnum tipoComprobante;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "cliente_nombre", nullable = false)
    private String clienteNombre;

    @Column(name = "cliente_cuit")
    private String clienteCuit;

    @Column(name = "cliente_direccion")
    private String clienteDireccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "cliente_condicion_iva")
    private CondicionIVAEnum clienteCondicionIVA;

    @Column(name = "subtotal")
    private Double subtotal;

    @Column(name = "total_iva")
    private Double totalIva;

    @Column(name = "total")
    private Double total;

    @Column(name = "cae", length = 14)
    private String cae;

    @Column(name = "cae_fecha_vencimiento")
    private LocalDate caeFechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private FacturaEstadoEnum estado;

    @Column(name = "mensaje_error", length = 500)
    private String mensajeError;

    @Column(name = "presupuesto_id")
    private Long presupuestoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago")
    private EstadoPagoEnum estadoPago = EstadoPagoEnum.PENDIENTE;

    @Column(name = "es_externa", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean esExterna = false;

    @Column(name = "archivo_nombre", length = 255)
    private String archivoNombre;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<FacturaItemDE> items = new ArrayList<>();

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
