package com.chemiconsult.entity;

import com.chemiconsult.enums.CondicionIVAEnum;
import com.chemiconsult.enums.ProvinciaEnum;
import com.chemiconsult.enums.TipoClienteEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "clientes")
@Data
public class ClienteDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Tipo de cliente ──
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoClienteEnum tipoCliente;

    // ── Persona Física ──
    private String nombre;
    private String apellido;
    private String dni;
    private String cuil;

    // ── Empresa ──
    private String razonSocial;
    private String cuit;

    // ── Compartidos ──
    @Column(nullable = false)
    private String email;

    private String telefono;
    private String celular;
    private String direccion;
    private String localidad;

    @Enumerated(EnumType.STRING)
    private ProvinciaEnum provincia;

    @Enumerated(EnumType.STRING)
    private CondicionIVAEnum condicionIVA;

    // ── Relación con usuario ──
    @OneToOne
    @JoinColumn(name = "user_id")
    private UserDE user; // nullable, se asigna después

    // ── Sistema ──
    @Column(nullable = false)
    private Boolean activo;

    private LocalDate createdDate;
    private LocalDate updateDate;
}
