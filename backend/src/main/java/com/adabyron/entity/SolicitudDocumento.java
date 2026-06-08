package com.adabyron.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_documentos")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SolicitudDocumento {

    // PK con el nombre exacto de tu tabla
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpostulacion_documento")
    private Integer idpostulacionDocumento;

    @ManyToOne
    @JoinColumn(name = "idsolicitud", nullable = false)
    private Solicitud solicitud;

    // Columna original de tu tabla
    @Column(name = "codocumento", nullable = false, length = 10)
    private String codocumento; // 'DOC001', 'DOC002', etc.

    // Columna original de tu tabla
    @Column(length = 200)
    private String comentario;

    // Columnas nuevas que agregamos con ALTER TABLE
    @Column(length = 50)
    private String tipo; // 'dni_estudiante', 'partida_nacimiento', etc.

    @Column(name = "nombre_archivo", length = 200)
    private String nombreArchivo;

    @Column(length = 500)
    private String ruta;

    private Long tamanio;

    @Column(length = 100)
    private String mimetype;

    @Builder.Default
    @Column(length = 10)
    private String estado = "PENDIENTE";

    @Builder.Default
    private LocalDateTime fechacarga = LocalDateTime.now();
}
