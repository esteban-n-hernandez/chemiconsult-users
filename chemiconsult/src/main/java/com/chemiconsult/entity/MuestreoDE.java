package com.chemiconsult.entity;

import com.chemiconsult.enums.EstadoMuestreoEnum;
import com.chemiconsult.enums.TipoEventoAgendaEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "MUESTREO_AGENDADO")
@Data
public class MuestreoDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO")
    private TipoEventoAgendaEnum tipo;

    @ManyToOne
    @JoinColumn(name = "CLIENTE_ID")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ClienteDE cliente;

    @ManyToOne
    @JoinColumn(name = "SUCURSAL_ID")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ClienteSucursalDE sucursal;

    @Column(name = "FECHA_HORA", nullable = false)
    private LocalDateTime fechaHora;

    @ManyToOne
    @JoinColumn(name = "RESPONSABLE_ID")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserDE responsable;

    @Column(name = "DIRECCION")
    private String direccion;

    @Column(name = "OBSERVACIONES")
    private String observaciones;

    @Column(name = "DOCUMENTACION")
    private String documentacion;

    @Column(name = "DE_QUIEN")
    private String deQuien;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false)
    private EstadoMuestreoEnum estado = EstadoMuestreoEnum.PENDIENTE;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;
}
