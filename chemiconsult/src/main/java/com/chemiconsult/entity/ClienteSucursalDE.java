package com.chemiconsult.entity;

import com.chemiconsult.enums.ProvinciaEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "CLIENTE_SUCURSAL")
@Data
public class ClienteSucursalDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CLIENTE_ID", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ClienteDE cliente;

    // Nombre para identificarla en selects: "Local Centro", "Depósito Norte", o la dirección misma
    @Column(name = "NOMBRE", nullable = false)
    private String nombre;

    @Column(name = "DIRECCION")
    private String direccion;

    @Column(name = "LOCALIDAD")
    private String localidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "PROVINCIA")
    private ProvinciaEnum provincia;

    @Column(name = "ACTIVO")
    private Boolean activo = true;

    @Column(name = "CREATED_DATE")
    private LocalDate createdDate;

    @Column(name = "UPDATE_DATE")
    private LocalDate updateDate;

    @OneToMany(mappedBy = "sucursal", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ClienteSucursalContactoDE> contactosAsociados;
}