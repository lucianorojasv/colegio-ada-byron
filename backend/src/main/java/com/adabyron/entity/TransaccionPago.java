package com.adabyron.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="transaccion_pago")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TransaccionPago {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer idtransaccion;

    @Column(nullable=false, unique=true, length=30)
    private String codigoTransaccion; // TXN-20260603-001

    @ManyToOne
    @JoinColumn(name="idestudiante", nullable=false)
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name="idpago")
    private Pago pago;

    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal monto;

    @Column(length=20)
    private String tipoTarjeta; // VISA, MASTERCARD, EFECTIVO, TRANSFERENCIA

    @Column(length=4)
    private String ultimosDigitos; // últimos 4 dígitos de la tarjeta

    @Column(length=100)
    private String nombreTitular;

    @Builder.Default
    @Column(nullable=false, length=15)
    private String estado = "PENDIENTE"; // PENDIENTE, APROBADO, RECHAZADO

    @Column(length=200)
    private String observaciones;

    @Builder.Default
    @Column
    private LocalDateTime fechaTransaccion = LocalDateTime.now();
}
