package com.adabyron.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="periodo_academico")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PeriodoAcademico {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer idperiodo;

    @Column(nullable=false, length=20)
    private String nombre; // "Bimestre 1", "Trimestre 2", etc.

    @Column(nullable=false, length=5)
    private String anio;

    @Column(nullable=false, length=20)
    private String tipo; // BIMESTRE, TRIMESTRE, SEMESTRE, ANUAL

    @Builder.Default
    @Column(nullable=false)
    private Boolean activo = true;
}
