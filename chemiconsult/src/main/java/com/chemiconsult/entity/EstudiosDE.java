package com.chemiconsult.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "estudios")
@Data
public class EstudiosDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-estudios")
    private UserDE user;
    private Date date;
    private String tipo;
    private String estado;

    @Lob
    @Column(name = "archivo")
    private byte[] archivo;


}
