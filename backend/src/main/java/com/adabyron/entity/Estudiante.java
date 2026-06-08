package com.adabyron.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name="estudiante")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Estudiante {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer idestudiante;

    @Column(nullable=false, length=80)
    private String paterno;

    @Column(nullable=false, length=80)
    private String materno;

    @Column(nullable=false, length=80)
    private String nombre;

    @Column(nullable=false)
    private LocalDate fechanac;

    @Column(unique=true, length=15)
    private String docidentidad;

    @Column(length=200)
    private String direccion;

    @Column(length=200)
    private String ubigeo;

    @Builder.Default
    @Column(nullable=false, length=10)
    private String estado = "ACTIVO";
}
