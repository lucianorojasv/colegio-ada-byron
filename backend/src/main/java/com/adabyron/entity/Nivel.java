// ============================================================
// ENTIDADES JPA - Colegio Ada Byron
// Archivo: entity/Entities.java (dividir en archivos separados)
// ============================================================

// ---- Nivel.java ----
package com.adabyron.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="nivel")
@Data @NoArgsConstructor @AllArgsConstructor
public class Nivel {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer idnivel;
    @Column(nullable=false, length=50)
    private String nombre;
}
