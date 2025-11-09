package com.chemiconsult.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "CHEMI_USERS")
@Data
public class UserDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USER_NAME")
    private String username;

    @Column(name = "EMAIL")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "PASSWORD_HASH")
    private String password;

    @Column(name = "ROLE")
    private String rol;

    @Column(name = "CREATED_DATE")
    private LocalDate createdDate;

    @Column(name = "UPDATE_DATE")
    private LocalDate updateDate;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-estudios")
    private List<EstudiosDE> estudios;

}
