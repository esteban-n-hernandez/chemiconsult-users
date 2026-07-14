package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "CLIENTE_SUCURSAL_CONTACTO")
@Data
public class ClienteSucursalContactoDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "SUCURSAL_ID", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ClienteSucursalDE sucursal;

    @ManyToOne
    @JoinColumn(name = "CONTACTO_ID", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ClienteContactoDE contacto;
}