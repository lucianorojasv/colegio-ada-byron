package com.adabyron.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PasarelaRequest {
    private Integer idestudiante;
    private BigDecimal monto;
    private String metodoPago; // TARJETA, YAPE, PLIN

    // Datos de tarjeta
    private String numeroTarjeta;
    private String cvv;
    private String fechaVencimiento;
    private String nombreTitular;
    private String apellidoTitular;
    private String correoTitular;
    private String tipoTarjeta;

    // Datos Yape
    private String celularYape;
    private String codigoYape;

    // Datos Plin
    private String celularPlin;
    private String bancoPlin; // INTERBANK, BBVA, SCOTIABANK

    // Para múltiples conceptos
    private List<Integer> conceptos;
}
