package com.adabyron.controller;

import com.adabyron.entity.Usuario;
import com.adabyron.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;

    // ── Listar todos ─────────────────────────────────────────
    @GetMapping
    public List<Usuario> listar() {
        List<Usuario> lista = repo.findAll();
        // Ocultar la clave antes de enviar
        lista.forEach(u -> u.setClave("***"));
        return lista;
    }

    // ── Buscar por ID ────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Integer id) {
        return repo.findById(id).map(u -> {
            u.setClave("***");
            return ResponseEntity.ok(u);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Crear usuario ────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Usuario usuario) {
        if (repo.existsByCodusuario(usuario.getCodusuario())) {
            return ResponseEntity.badRequest().body("El usuario '" + usuario.getCodusuario() + "' ya existe");
        }
        if (usuario.getClave() == null || usuario.getClave().length() < 4) {
            return ResponseEntity.badRequest().body("La contraseña debe tener al menos 4 caracteres");
        }
        usuario.setClave(passwordEncoder.encode(usuario.getClave()));
        Usuario guardado = repo.save(usuario);
        guardado.setClave("***");
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // ── Actualizar usuario ───────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id,
                                        @RequestBody Map<String, String> datos) {
        return repo.findById(id).map(u -> {
            if (datos.containsKey("rol") && datos.get("rol") != null) {
                u.setRol(datos.get("rol"));
            }
            if (datos.containsKey("codestado") && datos.get("codestado") != null) {
                u.setCodestado(datos.get("codestado"));
            }
            if (datos.containsKey("clave") && datos.get("clave") != null
                    && !datos.get("clave").isEmpty()) {
                u.setClave(passwordEncoder.encode(datos.get("clave")));
            }
            Usuario guardado = repo.save(u);
            guardado.setClave("***");
            return ResponseEntity.ok(guardado);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Cambiar estado (activar/desactivar) ──────────────────
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id,
                                            @RequestBody Map<String, String> body) {
        return repo.findById(id).map(u -> {
            u.setCodestado(body.getOrDefault("codestado", "INACTIVO"));
            repo.save(u);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Estado actualizado",
                "codestado", u.getCodestado()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Cambiar contraseña ───────────────────────────────────
    @PutMapping("/{id}/password")
    public ResponseEntity<?> cambiarPassword(@PathVariable Integer id,
                                              @RequestBody Map<String, String> body) {
        String nuevaClave = body.get("clave");
        if (nuevaClave == null || nuevaClave.length() < 4) {
            return ResponseEntity.badRequest()
                .body("La contraseña debe tener al menos 4 caracteres");
        }
        return repo.findById(id).map(u -> {
            u.setClave(passwordEncoder.encode(nuevaClave));
            repo.save(u);
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Eliminar usuario ─────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        return repo.findById(id).map(u -> {
            u.setCodestado("INACTIVO");
            repo.save(u);
            return ResponseEntity.ok(Map.of("mensaje", "Usuario desactivado"));
        }).orElse(ResponseEntity.notFound().build());
    }
}
