package com.chemiconsult.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "NUMERADOR_MUESTRA")
@Data
public class NumeradorMuestraDE {

    @Id
    private Long id;

    @Column(name = "VALOR_ACTUAL", nullable = false)
    private Long valorActual;
}
