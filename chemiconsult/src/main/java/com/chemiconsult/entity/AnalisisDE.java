package com.chemiconsult.entity;

import com.chemiconsult.enums.EstadoMuestraEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "ANALISIS")
@Data
public class AnalisisDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    @JsonBackReference("user-estudios")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserDE user;

    @Column(name = "ANALYSIS_TYPE")
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private EstadoMuestraEnum estado = EstadoMuestraEnum.PENDIENTE;

    @Column(name = "PDF_URL")
    private String archivoUrl;

    @Column(name = "CREATED_DATE")
    private LocalDate createdDate;

    @Column(name = "UPDATE_DATE")
    private LocalDate updateDate;

    @Column(name = "NUMERO_PROTOCOLO", unique = true)
    private String numeroProtocolo;

    @Column(name = "ID_MUESTRA")
    private String idMuestra;

    @Column(name = "PUNTO_MUESTREO")
    private String puntoMuestreo;

    @Column(name = "FECHA_INGRESO")
    private LocalDate fechaIngreso;

    @Column(name = "FECHA_ENTREGA")
    private LocalDate fechaEntrega;

    @ManyToOne
    @JoinColumn(name = "MATRIZ_ID", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MatrizDE matriz;

    @Column(name = "OBSERVACIONES")
    private String observaciones;

    @OneToMany(mappedBy = "analisis", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<AnalisisParametroDE> parametros;

    @OneToMany(mappedBy = "analisis", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<AnalisisResolucionDestinoDE> resolucionesAplicadas;
}