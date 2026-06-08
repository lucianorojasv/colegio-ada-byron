package com.adabyron.controller;

import com.adabyron.entity.GradoAcademico;
import com.adabyron.repository.GradoAcademicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/grados")
@RequiredArgsConstructor
public class GradoController {

    private final GradoAcademicoRepository repo;

    @GetMapping
    public List<GradoAcademico> listar() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<GradoAcademico> buscarPorId(@PathVariable Integer id) {
        return repo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nivel/{idnivel}")
    public List<GradoAcademico> porNivel(@PathVariable Integer idnivel) {
        return repo.findByNivelIdnivel(idnivel);
    }

    @PostMapping
    public ResponseEntity<GradoAcademico> crear(@RequestBody GradoAcademico g) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(g));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GradoAcademico> actualizar(@PathVariable Integer id,
                                                      @RequestBody GradoAcademico g) {
        return repo.findById(id).map(e -> {
            g.setIdgrado(id);
            return ResponseEntity.ok(repo.save(g));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
