package com.chemiconsult.entity;

import com.chemiconsult.enums.EstadoMuestreoEnum;
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

    @ManyToOne
    @JoinColumn(name = "CLIENTE_ID", nullable = false)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false)
    private EstadoMuestreoEnum estado = EstadoMuestreoEnum.PENDIENTE;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;
}
