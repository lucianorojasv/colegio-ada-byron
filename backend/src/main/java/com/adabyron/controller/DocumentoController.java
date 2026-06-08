package com.adabyron.controller;

import com.adabyron.entity.*;
import com.adabyron.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private static final Logger log = LoggerFactory.getLogger(DocumentoController.class);

    private final SolicitudDocumentoRepository docRepo;
    private final SolicitudRepository solicitudRepo;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // Tabla de codigos de documento (compatibilidad con tu tabla original)
    private static final Map<String, String> CODIGOS = Map.of(
        "dni_estudiante",       "DOC001",
        "dni_apoderado",        "DOC002",
        "partida_nacimiento",   "DOC003",
        "certificado_estudios", "DOC004",
        "constancia_siagie",    "DOC005",
        "vacunacion",           "DOC006"
    );

    // ── Subir documento ──────────────────────────────────────
    @PostMapping("/subir/{idsolicitud}")
    public ResponseEntity<?> subirDocumento(
            @PathVariable Integer idsolicitud,
            @RequestParam("tipo") String tipo,
            @RequestParam("archivo") MultipartFile archivo) {
        try {
            Solicitud solicitud = solicitudRepo.findById(idsolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

            // Crear carpeta uploads/solicitudes/{idsolicitud}/
            Path carpeta = Paths.get(uploadDir, "solicitudes",
                                     String.valueOf(idsolicitud));
            Files.createDirectories(carpeta);

            // Nombre único del archivo
            String nombreOriginal = archivo.getOriginalFilename();
            String ext = (nombreOriginal != null && nombreOriginal.contains("."))
                ? nombreOriginal.substring(nombreOriginal.lastIndexOf(".")) : "";
            String nombreGuardado = tipo + "_" + System.currentTimeMillis() + ext;

            // Guardar en disco
            Path rutaArchivo = carpeta.resolve(nombreGuardado);
            Files.copy(archivo.getInputStream(), rutaArchivo,
                       StandardCopyOption.REPLACE_EXISTING);

            String rutaRelativa = "solicitudes/" + idsolicitud + "/" + nombreGuardado;

            // Si ya existe un documento del mismo tipo, eliminarlo
            docRepo.findBySolicitudIdsolicitudAndTipo(idsolicitud, tipo)
                .forEach(d -> {
                    try {
                        Files.deleteIfExists(Paths.get(uploadDir, d.getRuta()));
                    } catch (IOException e) { /* ignorar */ }
                    docRepo.delete(d);
                });

            // Guardar en BD — usando tu columna codocumento
            SolicitudDocumento doc = SolicitudDocumento.builder()
                .solicitud(solicitud)
                .codocumento(CODIGOS.getOrDefault(tipo, "DOC999"))
                .tipo(tipo)
                .nombreArchivo(nombreOriginal)
                .ruta(rutaRelativa)
                .tamanio(archivo.getSize())
                .mimetype(archivo.getContentType())
                .estado("PENDIENTE")
                .build();

            SolicitudDocumento guardado = docRepo.save(doc);
            log.info("Doc subido: solicitud={} tipo={} cod={}",
                idsolicitud, tipo, doc.getCodocumento());

            return ResponseEntity.ok(Map.of(
                "id",           guardado.getIdpostulacionDocumento(),
                "tipo",         guardado.getTipo(),
                "codocumento",  guardado.getCodocumento(),
                "nombreArchivo",guardado.getNombreArchivo(),
                "tamanio",      guardado.getTamanio(),
                "estado",       guardado.getEstado(),
                "mensaje",      "Documento subido correctamente"
            ));

        } catch (Exception e) {
            log.error("Error al subir doc: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al subir: " + e.getMessage()));
        }
    }

    // ── Listar documentos de una solicitud ──────────────────
    @GetMapping("/solicitud/{idsolicitud}")
    public ResponseEntity<?> listar(@PathVariable Integer idsolicitud) {
        List<SolicitudDocumento> docs =
            docRepo.findBySolicitudIdsolicitud(idsolicitud);

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (SolicitudDocumento d : docs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("iddocumento",    d.getIdpostulacionDocumento());
            item.put("codocumento",    d.getCodocumento());
            item.put("tipo",           d.getTipo());
            item.put("nombreArchivo",  d.getNombreArchivo());
            item.put("tamanio",        d.getTamanio());
            item.put("mimetype",       d.getMimetype());
            item.put("estado",         d.getEstado());
            item.put("comentario",     d.getComentario());
            item.put("fechacarga",     d.getFechacarga());
            resultado.add(item);
        }
        return ResponseEntity.ok(resultado);
    }

    // ── Ver archivo en el navegador ──────────────────────────
    @GetMapping("/ver/{iddocumento}")
    public ResponseEntity<Resource> ver(@PathVariable Integer iddocumento) {
        try {
            SolicitudDocumento doc = docRepo.findById(iddocumento)
                .orElseThrow(() -> new RuntimeException("No encontrado"));

            Path rutaArchivo = Paths.get(uploadDir)
                .resolve(doc.getRuta()).normalize();
            Resource resource = new UrlResource(rutaArchivo.toUri());

            if (!resource.exists())
                return ResponseEntity.notFound().build();

            String ct = doc.getMimetype() != null
                ? doc.getMimetype() : "application/octet-stream";

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(ct))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + doc.getNombreArchivo() + "\"")
                .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── Secretaria: marcar CONFORME u OBSERVADO ──────────────
    @PutMapping("/{iddocumento}/revisar")
    public ResponseEntity<?> revisar(
            @PathVariable Integer iddocumento,
            @RequestBody Map<String, String> body) {

        return docRepo.findById(iddocumento).map(doc -> {
            doc.setEstado(body.getOrDefault("estado", "PENDIENTE"));
            if (body.containsKey("comentario"))
                doc.setComentario(body.get("comentario"));
            docRepo.save(doc);
            log.info("Doc revisado: id={} estado={}", iddocumento, doc.getEstado());
            return ResponseEntity.ok(Map.of(
                "mensaje", "Documento actualizado",
                "estado",  doc.getEstado()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Eliminar documento ───────────────────────────────────
    @DeleteMapping("/{iddocumento}")
    public ResponseEntity<?> eliminar(@PathVariable Integer iddocumento) {
        return docRepo.findById(iddocumento).map(doc -> {
            try {
                Files.deleteIfExists(Paths.get(uploadDir, doc.getRuta()));
            } catch (IOException e) {
                log.warn("No se pudo eliminar archivo: {}", doc.getRuta());
            }
            docRepo.delete(doc);
            return ResponseEntity.ok(Map.of("mensaje", "Documento eliminado"));
        }).orElse(ResponseEntity.notFound().build());
    }
}
