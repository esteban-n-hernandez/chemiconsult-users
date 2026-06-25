package com.chemiconsult.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

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
    private UserDE user;

    @Column(name = "ANALYSIS_TYPE")
    private String tipo;

    @Column(name = "STATUS")
    private String estado;

    @Column(name = "PDF", columnDefinition = "bytea")
    @Basic(fetch = FetchType.LAZY)
    private byte[] archivo;

    @Column(name = "CREATED_DATE")
    private LocalDate createdDate;

    @Column(name = "UPDATE_DATE")
    private LocalDate updateDate;

    @Column(name = "NUMERO_PROTOCOLO", unique = true)
    private String numeroProtocolo; // "CHQ-2026-014"

    @Column(name = "ID_MUESTRA")
    private String idMuestra; // "M-001"

    @ManyToOne
    @JoinColumn(name = "TIPO_MUESTRA_ID")
    private TipoMuestraDE tipoMuestra;

    @Column(name = "OBSERVACIONES")
    private String observaciones;

    @OneToMany(mappedBy = "analisis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnalisisParametroDE> parametros;

}
