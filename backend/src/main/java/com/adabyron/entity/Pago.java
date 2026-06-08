package com.adabyron.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="pago")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Pago {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer idpago;

    @ManyToOne
    @JoinColumn(name="idconceptopago", nullable=false)
    private ConceptoPago conceptoPago;

    @ManyToOne
    @JoinColumn(name="idestudiante", nullable=false)
    private Estudiante estudiante;

    @Builder.Default
    @Column
    private LocalDateTime fecharegisto = LocalDateTime.now();

    @Builder.Default
    @Column(nullable=false, length=8)
    private String codestado = "PEND";

    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal importe;

    @Column(length=50)
    private String metodopago;

    @Column(length=200)
    private String glosa;
}
