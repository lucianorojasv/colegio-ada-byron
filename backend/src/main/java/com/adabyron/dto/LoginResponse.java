package com.adabyron.dto;

import lombok.*;

@Data @AllArgsConstructor
public class LoginResponse {
    private String token;
    private String codusuario;
    private String rol;
    private String mensaje;
}
