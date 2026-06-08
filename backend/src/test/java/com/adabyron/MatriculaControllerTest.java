package com.adabyron;

// ============================================================
// MatriculaControllerTest.java
// Pruebas de integración del controlador de matrículas
// ============================================================

import com.adabyron.entity.*;
import com.adabyron.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MatriculaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired EstudianteRepository estudianteRepo;

    // ── Test: Listar matrículas ──────────────────────────────
    @Test @Order(1)
    @WithMockUser(roles = "ADMIN")
    void listarMatriculas_debeRetornar200() throws Exception {
        mockMvc.perform(get("/api/matriculas").param("anio", "2026"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // ── Test: Stats del dashboard ────────────────────────────
    @Test @Order(2)
    @WithMockUser(roles = "ADMIN")
    void getStats_debeRetornarCamposEsperados() throws Exception {
        mockMvc.perform(get("/api/matriculas/stats").param("anio", "2026"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalMatriculas").exists())
               .andExpect(jsonPath("$.pendientes").exists())
               .andExpect(jsonPath("$.totalAlumnos").exists());
    }

    // ── Test: Matrícula sin autenticación debe rechazarse ────
    @Test @Order(3)
    void listarMatriculas_sinAuth_debeRetornar401() throws Exception {
        mockMvc.perform(get("/api/matriculas"))
               .andExpect(status().isUnauthorized());
    }
}
