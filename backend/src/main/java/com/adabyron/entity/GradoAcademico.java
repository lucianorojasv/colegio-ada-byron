package com.adabyron.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="grado_academico")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GradoAcademico {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer idgrado;

    @Column(nullable=false, length=50)
    private String nombre;

    @ManyToOne
    @JoinColumn(name="idnivel", nullable=false)
    private Nivel nivel;
}
