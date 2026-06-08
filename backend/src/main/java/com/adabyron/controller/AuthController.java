package com.adabyron.controller;

import com.adabyron.dto.*;
import com.adabyron.entity.Usuario;
import com.adabyron.repository.UsuarioRepository;
import com.adabyron.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        log.info("Intento de login: usuario={}", req.getCodusuario());
        return usuarioRepo.findByCodusuario(req.getCodusuario())
            .filter(u -> u.getCodestado().equals("ACTIVO"))
            .filter(u -> passwordEncoder.matches(req.getClave(), u.getClave()))
            .map(u -> {
                String token = jwtUtil.generateToken(u.getCodusuario(), u.getRol());
                log.info("Login exitoso: usuario={} rol={}", u.getCodusuario(), u.getRol());
                return ResponseEntity.ok(new LoginResponse(token, u.getCodusuario(), u.getRol(), "Login exitoso"));
            })
            .orElseGet(() -> {
                log.warn("Login fallido: usuario={}", req.getCodusuario());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(null, null, null, "Credenciales invalidas"));
            });
    }

    @GetMapping("/setup")
    public ResponseEntity<String> setup() {
        log.info("Ejecutando setup de usuario admin");
        if (usuarioRepo.existsByCodusuario("admin")) {
            usuarioRepo.findByCodusuario("admin").ifPresent(u -> {
                u.setClave(passwordEncoder.encode("1234"));
                u.setCodestado("ACTIVO");
                u.setRol("ADMIN");
                usuarioRepo.save(u);
            });
            log.info("Admin actualizado. Clave: 1234");
            return ResponseEntity.ok("Admin actualizado. Usuario: admin / Clave: 1234");
        }
        Usuario admin = Usuario.builder()
            .codusuario("admin")
            .clave(passwordEncoder.encode("1234"))
            .codestado("ACTIVO")
            .rol("ADMIN")
            .build();
        usuarioRepo.save(admin);
        log.info("Admin creado correctamente");
        return ResponseEntity.ok("Admin creado. Usuario: admin / Clave: 1234");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        log.info("Registro de usuario: {}", usuario.getCodusuario());
        if (usuarioRepo.existsByCodusuario(usuario.getCodusuario())) {
            return ResponseEntity.badRequest().body("Usuario ya existe");
        }
        usuario.setClave(passwordEncoder.encode(usuario.getClave()));
        return ResponseEntity.ok(usuarioRepo.save(usuario));
    }
}
