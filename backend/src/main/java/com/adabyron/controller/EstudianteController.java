package com.adabyron.controller;

import com.adabyron.entity.Estudiante;
import com.adabyron.repository.EstudianteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/estudiantes")
@RequiredArgsConstructor
public class EstudianteController {

    private final EstudianteRepository repo;

    @GetMapping
    public List<Estudiante> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Estudiante> buscarPorId(@PathVariable Integer id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public List<Estudiante> buscar(@RequestParam String q) {
        return repo.buscar(q);
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<Estudiante> buscarPorDni(@PathVariable String dni) {
        return repo.findByDocidentidad(dni)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Estudiante> crear(@Valid @RequestBody Estudiante e) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(e));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Estudiante> actualizar(@PathVariable Integer id,
                                                  @Valid @RequestBody Estudiante e) {
        return repo.findById(id).map(existing -> {
            e.setIdestudiante(id);
            return ResponseEntity.ok(repo.save(e));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        return repo.findById(id).map(e -> {
            e.setEstado("INACTIVO");
            repo.save(e);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
