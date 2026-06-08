package com.adabyron.controller;

import com.adabyron.entity.*;
import com.adabyron.repository.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/secciones")
@RequiredArgsConstructor
public class SeccionController {

    private final SeccionRepository seccionRepo;
    private final MatriculaRepository matriculaRepo;

    @GetMapping
    public List<Seccion> listar() { return seccionRepo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Seccion> buscarPorId(@PathVariable Integer id) {
        return seccionRepo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/grado/{idgrado}")
    public List<Seccion> porGrado(@PathVariable Integer idgrado) {
        return seccionRepo.findByGradoIdgrado(idgrado);
    }

    @GetMapping("/{id}/vacantes")
    public ResponseEntity<Map<String,Object>> vacantes(@PathVariable Integer id,
                                                        @RequestParam(defaultValue="2026") String anio) {
        return seccionRepo.findById(id).map(s -> {
            long ocupados = seccionRepo.contarMatriculadosPorSeccion(id, anio);
            Map<String,Object> resp = new LinkedHashMap<>();
            resp.put("capacidad", s.getCapacidad());
            resp.put("ocupados", ocupados);
            resp.put("disponibles", s.getCapacidad() - ocupados);
            return ResponseEntity.ok(resp);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Seccion> crear(@RequestBody Seccion s) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seccionRepo.save(s));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Seccion> actualizar(@PathVariable Integer id, @RequestBody Seccion s) {
        return seccionRepo.findById(id).map(e -> {
            s.setIdseccion(id);
            return ResponseEntity.ok(seccionRepo.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        seccionRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
