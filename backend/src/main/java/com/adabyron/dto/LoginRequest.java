// ============================================================
// DTOs - Data Transfer Objects
// ============================================================

// LoginRequest.java
package com.adabyron.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank private String codusuario;
    @NotBlank private String clave;
}
