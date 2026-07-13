package com.chemiconsult.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "RESOLUCION")
@Data
public class ResolucionDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NOMBRE", nullable = false, unique = true)
    private String nombre; // "Res 336/06", "Res 283/19", "CAA", "Ley 19587 - Decreto 351/79"

    @Column(name = "DESCRIPCION")
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "MATRIZ_ID", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MatrizDE matriz;

    @Column(name = "TIENE_DESTINO", nullable = false)
    private Boolean tieneDestino = true; // false para CAA / Ley 19587 (límite único)

    @OneToMany(mappedBy = "resolucion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ResolucionDestinoDE> destinos;
}