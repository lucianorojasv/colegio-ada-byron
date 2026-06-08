package com.adabyron.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="usuarios")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer idusuario;

    @Column(nullable=false, unique=true, length=50)
    private String codusuario;

    @Column(nullable=false, length=200)
    private String clave;

    @Builder.Default
    @Column(nullable=false, length=8)
    private String codestado = "ACTIVO";

    @Builder.Default
    @Column(nullable=false, length=30)
    private String rol = "PADRE";
}
