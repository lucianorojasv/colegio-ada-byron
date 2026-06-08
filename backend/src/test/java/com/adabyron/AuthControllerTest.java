package com.adabyron;

// ============================================================
// AuthControllerTest.java
// Pruebas del módulo de autenticación JWT
// ============================================================

import com.adabyron.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // ── Login con credenciales correctas ─────────────────────
    @Test @Order(1)
    void login_credencialesCorrectas_debeRetornarToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setCodusuario("admin");
        req.setClave("admin123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    // ── Login con credenciales incorrectas ───────────────────
    @Test @Order(2)
    void login_credencialesIncorrectas_debeRetornar401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setCodusuario("admin");
        req.setClave("claveIncorrecta");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    // ── Login con usuario inexistente ────────────────────────
    @Test @Order(3)
    void login_usuarioInexistente_debeRetornar401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setCodusuario("noexiste@test.com");
        req.setClave("cualquiera");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    // ── Acceso sin token debe ser rechazado ──────────────────
    @Test @Order(4)
    void accesoSinToken_debeRetornar401() throws Exception {
        mockMvc.perform(get("/api/estudiantes"))
               .andExpect(status().isUnauthorized());
    }
}
