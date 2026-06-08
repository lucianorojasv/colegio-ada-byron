package com.adabyron.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="apoderado")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Apoderado {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer idapoderado;

    @Column(nullable=false, length=80)
    private String paterno;

    @Column(nullable=false, length=80)
    private String materno;

    @Column(nullable=false, length=80)
    private String nombre;

    @Column(length=15)
    private String telefono;

    @Column(length=15)
    private String celular;

    @Column(length=100)
    private String correo;

    @Column(length=15)
    private String docidentidad;
}
