package com.chemiconsult.entity;

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
    private String nombre; // Ej: "Colectora Cloacal", "Conducto Pluvial", "Agua Superficial"

    // Relación con la tabla intermedia que define qué parámetros y límites tiene este destino
    @OneToMany(mappedBy = "destino", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResolucionDestinoParametroDE> parametrosConfigurados;
}