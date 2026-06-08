package com.adabyron.controller;

import com.adabyron.entity.Pago;
import com.adabyron.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoRepository pagoRepo;

    @GetMapping
    public List<Pago> listar() { return pagoRepo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Pago> buscarPorId(@PathVariable Integer id) {
        return pagoRepo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estudiante/{idestudiante}")
    public List<Pago> porEstudiante(@PathVariable Integer idestudiante) {
        return pagoRepo.findByEstudianteIdestudiante(idestudiante);
    }

    @PostMapping
    public ResponseEntity<Pago> crear(@RequestBody Pago p) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoRepo.save(p));
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<Pago> confirmar(@PathVariable Integer id) {
        return pagoRepo.findById(id).map(p -> {
            p.setCodestado("PAGADO");
            return ResponseEntity.ok(pagoRepo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }
}
