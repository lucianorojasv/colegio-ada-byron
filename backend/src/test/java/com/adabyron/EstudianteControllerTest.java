package com.adabyron;

import com.adabyron.entity.Estudiante;
import com.adabyron.repository.EstudianteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EstudianteControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired EstudianteRepository repo;

    static Integer idCreado;

    @Test @Order(1)
    @WithMockUser(roles = "ADMIN")
    void crearEstudiante_datosValidos_debeRetornar201() throws Exception {
        Estudiante e = Estudiante.builder()
            .paterno("Garcia").materno("Lopez")
            .nombre("Juan Carlos").fechanac(LocalDate.of(2012, 3, 15))
            .docidentidad("12345678").estado("ACTIVO").build();

        String resp = mockMvc.perform(post("/api/estudiantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(e)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.idestudiante").exists())
            .andExpect(jsonPath("$.nombre").value("Juan Carlos"))
            .andReturn().getResponse().getContentAsString();

        Estudiante creado = objectMapper.readValue(resp, Estudiante.class);
        idCreado = creado.getIdestudiante();
        assertThat(idCreado).isNotNull().isGreaterThan(0);
    }

    @Test @Order(2)
    @WithMockUser(roles = "ADMIN")
    void listarEstudiantes_debeRetornarLista() throws Exception {
        mockMvc.perform(get("/api/estudiantes"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(0)));
    }

    @Test @Order(3)
    @WithMockUser(roles = "ADMIN")
    void buscarPorDni_existente_debeRetornarEstudiante() throws Exception {
        if (idCreado == null) return;
        mockMvc.perform(get("/api/estudiantes/dni/12345678"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.docidentidad").value("12345678"));
    }

    @Test @Order(4)
    @WithMockUser(roles = "ADMIN")
    void buscarPorDni_inexistente_debeRetornar404() throws Exception {
        mockMvc.perform(get("/api/estudiantes/dni/99999999"))
               .andExpect(status().isNotFound());
    }

    @Test @Order(5)
    @WithMockUser(roles = "ADMIN")
    void actualizarEstudiante_debeRetornar200() throws Exception {
        if (idCreado == null) return;
        Estudiante e = Estudiante.builder()
            .paterno("Garcia").materno("Lopez")
            .nombre("Juan Carlos Actualizado")
            .fechanac(LocalDate.of(2012, 3, 15))
            .docidentidad("12345678").estado("ACTIVO").build();

        mockMvc.perform(put("/api/estudiantes/" + idCreado)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(e)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Juan Carlos Actualizado"));
    }

    @Test @Order(6)
    @WithMockUser(roles = "ADMIN")
    void eliminarEstudiante_debeDesactivar() throws Exception {
        if (idCreado == null) return;
        mockMvc.perform(delete("/api/estudiantes/" + idCreado))
               .andExpect(status().isOk());

        Estudiante e = repo.findById(idCreado).orElseThrow();
        assertThat(e.getEstado()).isEqualTo("INACTIVO");
    }
}
