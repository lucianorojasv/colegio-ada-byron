package com.adabyron.dto;

import lombok.*;
import java.time.LocalDate;

// DTO para registro completo de solicitud de matrícula por el padre
@Data
public class SolicitudRequest {

    // Datos del apoderado
    private String apoderadoPaterno;
    private String apoderadoMaterno;
    private String apoderadoNombre;
    private String apoderadoCelular;
    private String apoderadoCorreo;
    private String apoderadoDni;

    // Datos del estudiante
    private String estudiantePaterno;
    private String estudianteMaterno;
    private String estudianteNombre;
    private LocalDate estudianteFechanac;
    private String estudianteDni;
    private String estudianteDireccion;

    // Datos de matrícula
    private Integer idseccion;
    private String aniolectivo;
    private String comentario;
}
