package com.adabyron.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="solicitud")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Solicitud {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer idsolicitud;

    @Builder.Default
    @Column
    private LocalDateTime fecharegisto = LocalDateTime.now();

    @Builder.Default
    @Column(nullable=false, length=6)
    private String codestado = "PEND";

    @ManyToOne
    @JoinColumn(name="idestudiante", nullable=false)
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name="idapoderado", nullable=false)
    private Apoderado apoderado;

    @ManyToOne
    @JoinColumn(name="idseccion")
    private Seccion seccion;

    @Column(length=200)
    private String comentario;

    @Column(length=5)
    private String aniolectivo;
}
