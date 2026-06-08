package com.adabyron.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="seccion")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Seccion {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer idseccion;

    @Column(nullable=false, length=50)
    private String nombre;

    @Builder.Default
    @Column(nullable=false)
    private Integer capacidad = 30;

    @Column(nullable=false, length=20)
    private String turno;

    @Column(length=20)
    private String aula;

    @ManyToOne
    @JoinColumn(name="idgrado", nullable=false)
    private GradoAcademico grado;
}
