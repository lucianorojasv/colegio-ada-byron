package com.adabyron.controller;

import com.adabyron.dto.SolicitudRequest;
import com.adabyron.entity.*;
import com.adabyron.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudRepository solicitudRepo;
    private final EstudianteRepository estudianteRepo;
    private final ApoderadoRepository apoderadoRepo;
    private final SeccionRepository seccionRepo;
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    // ── PADRE: Registrar nueva solicitud de matrícula ────────
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarSolicitud(@RequestBody SolicitudRequest req) {
        try {
            // 1. Buscar o crear apoderado
            Apoderado apoderado = apoderadoRepo.findByDocidentidad(req.getApoderadoDni())
                .orElseGet(() -> apoderadoRepo.save(
                    Apoderado.builder()
                        .paterno(req.getApoderadoPaterno())
                        .materno(req.getApoderadoMaterno())
                        .nombre(req.getApoderadoNombre())
                        .celular(req.getApoderadoCelular())
                        .correo(req.getApoderadoCorreo())
                        .docidentidad(req.getApoderadoDni())
                        .build()
                ));

            // 2. Buscar o crear estudiante
            Estudiante estudiante = (req.getEstudianteDni() != null && !req.getEstudianteDni().isEmpty())
                ? estudianteRepo.findByDocidentidad(req.getEstudianteDni())
                    .orElseGet(() -> estudianteRepo.save(
                        Estudiante.builder()
                            .paterno(req.getEstudiantePaterno())
                            .materno(req.getEstudianteMaterno())
                            .nombre(req.getEstudianteNombre())
                            .fechanac(req.getEstudianteFechanac())
                            .docidentidad(req.getEstudianteDni())
                            .direccion(req.getEstudianteDireccion())
                            .build()
                    ))
                : estudianteRepo.save(
                    Estudiante.builder()
                        .paterno(req.getEstudiantePaterno())
                        .materno(req.getEstudianteMaterno())
                        .nombre(req.getEstudianteNombre())
                        .fechanac(req.getEstudianteFechanac())
                        .docidentidad(req.getEstudianteDni())
                        .direccion(req.getEstudianteDireccion())
                        .build()
                );

            // 3. Verificar vacantes si se especificó sección
            Seccion seccion = null;
            if (req.getIdseccion() != null) {
                seccion = seccionRepo.findById(req.getIdseccion()).orElse(null);
                if (seccion != null) {
                    long ocupados = seccionRepo.contarMatriculadosPorSeccion(
                        req.getIdseccion(), req.getAniolectivo());
                    if (ocupados >= seccion.getCapacidad()) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("error", "No hay vacantes disponibles en esa sección"));
                    }
                }
            }

            // 4. Crear la solicitud
            Solicitud solicitud = Solicitud.builder()
                .estudiante(estudiante)
                .apoderado(apoderado)
                .seccion(seccion)
                .aniolectivo(req.getAniolectivo() != null ? req.getAniolectivo() : "2026")
                .comentario(req.getComentario())
                .codestado("PEND")
                .fecharegisto(LocalDateTime.now())
                .build();

            Solicitud guardada = solicitudRepo.save(solicitud);

            // 5. Crear usuario para el padre si no existe
            if (req.getApoderadoCorreo() != null && !usuarioRepo.existsByCodusuario(req.getApoderadoCorreo())) {
                String claveTemporal = req.getApoderadoDni() != null ? req.getApoderadoDni() : "padre123";
                usuarioRepo.save(Usuario.builder()
                    .codusuario(req.getApoderadoCorreo())
                    .clave(passwordEncoder.encode(claveTemporal))
                    .codestado("ACTIVO")
                    .rol("PADRE")
                    .build());
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("idsolicitud", guardada.getIdsolicitud());
            resp.put("estado", guardada.getCodestado());
            resp.put("mensaje", "Solicitud registrada exitosamente. Estado: PENDIENTE de revisión.");
            resp.put("estudiante", estudiante.getNombre() + " " + estudiante.getPaterno());
            resp.put("fechaRegistro", guardada.getFecharegisto());
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al registrar: " + e.getMessage()));
        }
    }

    // ── PADRE: Consultar estado de su solicitud ──────────────
    @GetMapping("/estado/{idsolicitud}")
    public ResponseEntity<?> consultarEstado(@PathVariable Integer idsolicitud) {
        return solicitudRepo.findById(idsolicitud)
            .map(s -> {
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("idsolicitud", s.getIdsolicitud());
                resp.put("estado", s.getCodestado());
                resp.put("estadoDescripcion", getEstadoDescripcion(s.getCodestado()));
                resp.put("estudiante", s.getEstudiante().getNombre() + " " + s.getEstudiante().getPaterno());
                resp.put("seccion", s.getSeccion() != null ? s.getSeccion().getNombre() : "Por asignar");
                resp.put("grado", s.getSeccion() != null ? s.getSeccion().getGrado().getNombre() : "Por asignar");
                resp.put("fechaRegistro", s.getFecharegisto());
                resp.put("comentario", s.getComentario());
                return ResponseEntity.ok(resp);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // ── PADRE: Mis solicitudes por apoderado ─────────────────
    @GetMapping("/mis-solicitudes/{idapoderado}")
    public ResponseEntity<?> misSolicitudes(@PathVariable Integer idapoderado) {
        return ResponseEntity.ok(
            solicitudRepo.findByApoderadoIdapoderadoOrderByFecharegistoDesc(idapoderado)
        );
    }

    // ── SECRETARIA: Listar todas las solicitudes ─────────────
    @GetMapping
    public ResponseEntity<?> listarTodas() {
        return ResponseEntity.ok(solicitudRepo.findAllByOrderByFecharegistoDesc());
    }

    // ── SECRETARIA: Listar por estado ────────────────────────
    @GetMapping("/estado-filtro/{estado}")
    public ResponseEntity<?> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(
            solicitudRepo.findByCodestadoOrderByFecharegistoDesc(estado)
        );
    }

    // ── SECRETARIA: Aprobar solicitud ────────────────────────
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Integer id,
                                      @RequestBody(required=false) Map<String,String> body) {
        return solicitudRepo.findById(id).map(s -> {
            s.setCodestado("APRO");
            s.setComentario(body != null ? body.getOrDefault("comentario", s.getComentario()) : s.getComentario());
            solicitudRepo.save(s);
            return ResponseEntity.ok(Map.of("mensaje", "Solicitud aprobada", "estado", "APRO"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── SECRETARIA: Observar solicitud ───────────────────────
    @PutMapping("/{id}/observar")
    public ResponseEntity<?> observar(@PathVariable Integer id,
                                       @RequestBody Map<String,String> body) {
        return solicitudRepo.findById(id).map(s -> {
            s.setCodestado("OBS");
            s.setComentario(body.getOrDefault("comentario", "Documentación incompleta"));
            solicitudRepo.save(s);
            return ResponseEntity.ok(Map.of("mensaje", "Solicitud observada", "estado", "OBS"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── SECRETARIA: Rechazar solicitud ───────────────────────
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazar(@PathVariable Integer id,
                                       @RequestBody Map<String,String> body) {
        return solicitudRepo.findById(id).map(s -> {
            s.setCodestado("REC");
            s.setComentario(body.getOrDefault("comentario", "Solicitud rechazada"));
            solicitudRepo.save(s);
            return ResponseEntity.ok(Map.of("mensaje", "Solicitud rechazada", "estado", "REC"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── SECRETARIA: Ver detalle de solicitud ─────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(@PathVariable Integer id) {
        return solicitudRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    private String getEstadoDescripcion(String cod) {
        return switch (cod) {
            case "PEND" -> "Pendiente de revisión";
            case "APRO" -> "Aprobada - Proceder con el pago";
            case "OBS"  -> "Observada - Revisar documentación";
            case "REC"  -> "Rechazada";
            default     -> cod;
        };
    }
}
