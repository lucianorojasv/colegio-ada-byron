package com.adabyron.controller;

import com.adabyron.dto.MatriculaRequest;
import com.adabyron.entity.*;
import com.adabyron.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/matriculas")
@RequiredArgsConstructor
public class MatriculaController {

    private final MatriculaRepository matriculaRepo;
    private final EstudianteRepository estudianteRepo;
    private final SeccionRepository seccionRepo;
    private final PagoRepository pagoRepo;
    private final ConceptoPagoRepository conceptoPagoRepo;

    @GetMapping
    public List<Matricula> listar(@RequestParam(defaultValue="2026") String anio) {
        return matriculaRepo.findByAniolectivo(anio);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Matricula> buscarPorId(@PathVariable Integer id) {
        return matriculaRepo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estudiante/{idestudiante}")
    public List<Matricula> porEstudiante(@PathVariable Integer idestudiante) {
        return matriculaRepo.findByEstudianteIdestudiante(idestudiante);
    }

    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody MatriculaRequest req) {
        // 1. Verificar que el estudiante existe
        Estudiante estudiante = estudianteRepo.findById(req.getIdestudiante())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        // 2. Verificar que ya no esté matriculado este año
        Optional<Matricula> yaMatriculado = matriculaRepo
                .findByEstudianteIdestudianteAndAniolectivo(req.getIdestudiante(), req.getAniolectivo());
        if (yaMatriculado.isPresent() && yaMatriculado.get().getCodestado().equals("ACTIVO")) {
            return ResponseEntity.badRequest().body("El estudiante ya está matriculado en " + req.getAniolectivo());
        }

        // 3. Verificar vacantes disponibles
        Seccion seccion = seccionRepo.findById(req.getIdseccion())
                .orElseThrow(() -> new RuntimeException("Sección no encontrada"));
        long ocupados = seccionRepo.contarMatriculadosPorSeccion(req.getIdseccion(), req.getAniolectivo());
        if (ocupados >= seccion.getCapacidad()) {
            return ResponseEntity.badRequest().body("No hay vacantes disponibles en esta sección");
        }

        // 4. Registrar el pago
        ConceptoPago concepto = conceptoPagoRepo.findById(1)
                .orElseThrow(() -> new RuntimeException("Concepto de pago no encontrado"));
        Pago pago = Pago.builder()
                .conceptoPago(concepto)
                .estudiante(estudiante)
                .importe(req.getMontoMatricula())
                .metodopago(req.getMetodopago())
                .codestado("PAGADO")
                .fecharegisto(LocalDateTime.now())
                .build();
        pago = pagoRepo.save(pago);

        // 5. Registrar la matrícula
        Matricula matricula = Matricula.builder()
                .aniolectivo(req.getAniolectivo())
                .estudiante(estudiante)
                .seccion(seccion)
                .pago(pago)
                .fecharegisto(LocalDateTime.now())
                .codestado("ACTIVO")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(matriculaRepo.save(matricula));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Matricula> cambiarEstado(@PathVariable Integer id,
                                                    @RequestParam String estado) {
        return matriculaRepo.findById(id).map(m -> {
            m.setCodestado(estado);
            return ResponseEntity.ok(matriculaRepo.save(m));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Dashboard stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String,Object>> stats(@RequestParam(defaultValue="2026") String anio) {
        Map<String,Object> stats = new LinkedHashMap<>();
        stats.put("totalMatriculas", matriculaRepo.totalActivasPorAnio(anio));
        stats.put("pendientes", matriculaRepo.totalPendientesPorAnio(anio));
        stats.put("totalIngresos", matriculaRepo.totalIngresosPorAnio(anio));
        stats.put("totalAlumnos", estudianteRepo.count());
        return ResponseEntity.ok(stats);
    }
}
