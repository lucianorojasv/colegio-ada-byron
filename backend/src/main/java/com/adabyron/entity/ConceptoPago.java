package com.adabyron.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="concepto_pago")
@Data @NoArgsConstructor @AllArgsConstructor
public class ConceptoPago {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer idconceptopago;

    @Column(nullable=false, length=100)
    private String nombre;
}
