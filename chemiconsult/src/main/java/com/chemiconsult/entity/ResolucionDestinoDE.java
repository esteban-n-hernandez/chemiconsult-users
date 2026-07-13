package com.chemiconsult.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "RESOL_DESTINO")
@Data
public class ResolucionDestinoDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "RESOLUCION_ID", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ResolucionDE resolucion;

    @Column(name = "NOMBRE", nullable = false)
    private String nombre; // "Colectora Cloacal", "Cond. Pluvial...", "Absorción por suelo", "Único"

    @OneToMany(mappedBy = "destino", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ResolucionDestinoParametroDE> parametrosConfigurados;
}