package com.adabyron.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MatriculaRequest {
    @NotNull  private Integer idestudiante;
    @NotNull  private Integer idseccion;
    @NotBlank private String aniolectivo;
    @NotNull  private BigDecimal montoMatricula;
    @NotBlank private String metodopago;
}

// ---- EstudianteDTO.java ----
class EstudianteDTO {
    public Integer idestudiante;
    public String paterno;
    public String materno;
    public String nombre;
    public LocalDate fechanac;
    public String docidentidad;
    public String direccion;
    public String estado;
}

// ---- DashboardDTO.java ----
class DashboardDTO {
    public long totalAlumnos;
    public long totalMatriculas;
    public long matriculasPendientes;
    public long seccionesActivas;
    public double totalIngresos;
    public int gradosActivos;
}
