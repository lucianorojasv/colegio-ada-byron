package com.adabyron.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="matricula")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Matricula {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer idmatricula;

    @Column(nullable=false, length=5)
    private String aniolectivo;

    @Builder.Default
    @Column
    private LocalDateTime fecharegisto = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name="idestudiante", nullable=false)
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name="idseccion", nullable=false)
    private Seccion seccion;

    @ManyToOne
    @JoinColumn(name="idpago")
    private Pago pago;

    @Builder.Default
    @Column(nullable=false, length=10)
    private String codestado = "ACTIVO";
}
