package com.chemiconsult.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "ANALISIS")
@Data
public class EstudiosDE {

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

    @Lob
    @Column(name = "PDF", columnDefinition = "bytea")
    private byte[] archivo;


    @Column(name = "CREATED_DATE")
    private LocalDate createdDate;

    @Column(name = "UPDATE_DATE")
    private LocalDate updateDate;

}
