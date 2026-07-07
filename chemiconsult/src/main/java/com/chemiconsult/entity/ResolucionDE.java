package com.chemiconsult.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "RESOLUCION")
@Data
public class ResolucionDE {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NOMBRE", nullable = false, unique = true)
    private String nombre; // Ej: "Res 336/06", "Ley 24051", "CAA Art. 982"

    @Column(name = "DESCRIPCION")
    private String descripcion;

    @OneToMany(mappedBy = "resolucion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResolucionDestinoDE> destinos;
}