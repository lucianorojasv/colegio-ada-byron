package com.adabyron.controller;

import com.adabyron.dto.PasarelaRequest;
import com.adabyron.entity.*;
import com.adabyron.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Preconditions;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/pagos/pasarela")
@RequiredArgsConstructor
public class PasarelaPagoController {

    private static final Logger log = LoggerFactory.getLogger(PasarelaPagoController.class);

    private final PagoRepository         pagoRepo;
    private final EstudianteRepository   estudianteRepo;
    private final ConceptoPagoRepository conceptoRepo;
    private final TransaccionPagoRepository transaccionRepo;

    // ── Procesar pago (tarjeta / Yape / Plin) ───────────────
    @PostMapping("/procesar")
    public ResponseEntity<?> procesarPago(@RequestBody PasarelaRequest req) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            String metodo = req.getMetodoPago() != null
                ? req.getMetodoPago().toUpperCase() : "TARJETA";

            // Validar según método
            Map<String, Object> validacion = switch (metodo) {
                case "YAPE"  -> validarYape(req);
                case "PLIN"  -> validarPlin(req);
                default      -> validarTarjeta(req);
            };

            if (!(boolean) validacion.get("valida")) {
                resp.put("success", false);
                resp.put("mensaje", validacion.get("error"));
                return ResponseEntity.badRequest().body(resp);
            }

            // Buscar estudiante
            Estudiante est = estudianteRepo.findById(req.getIdestudiante())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

            // Código único de transacción
            String codigo = "TXN-"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%04d", (int)(Math.random() * 9999 + 1));

            // Simular aprobación
            boolean aprobado = simularAprobacion(req, metodo);
            String estadoTxn = aprobado ? "APROBADO" : "RECHAZADO";

            // Guardar transacción
            TransaccionPago txn = TransaccionPago.builder()
                .codigoTransaccion(codigo)
                .estudiante(est)
                .monto(req.getMonto())
                .tipoTarjeta(getTipoTarjeta(req, metodo))
                .ultimosDigitos(getUltimosDigitos(req, metodo))
                .nombreTitular(getNombreTitular(req, metodo))
                .estado(estadoTxn)
                .observaciones(getObservacion(aprobado, metodo))
                .fechaTransaccion(LocalDateTime.now())
                .build();
            transaccionRepo.save(txn);

            if (!aprobado) {
                resp.put("success", false);
                resp.put("codigoTransaccion", codigo);
                resp.put("mensaje", getMensajeRechazo(metodo));
                return ResponseEntity.ok(resp);
            }

            // Registrar pago
            ConceptoPago concepto = conceptoRepo.findById(1).orElse(null);
            Pago pago = Pago.builder()
                .estudiante(est)
                .conceptoPago(concepto)
                .importe(req.getMonto())
                .metodopago(metodo)
                .codestado("PAGADO")
                .glosa("Transacción: " + codigo + " | Método: " + metodo)
                .build();
            pagoRepo.save(pago);
            txn.setPago(pago); transaccionRepo.save(txn);

            // Respuesta de éxito
            resp.put("success",           true);
            resp.put("codigoTransaccion", codigo);
            resp.put("metodoPago",        metodo);
            resp.put("tipoTarjeta",       txn.getTipoTarjeta());
            resp.put("ultimosDigitos",    txn.getUltimosDigitos());
            resp.put("nombreTitular",     txn.getNombreTitular());
            resp.put("monto",             req.getMonto());
            resp.put("estudiante",        est.getNombre() + " " + est.getPaterno());
            resp.put("fecha", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            resp.put("mensaje",           "Pago procesado exitosamente");
            resp.put("idpago",            pago.getIdpago());
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            resp.put("success", false);
            resp.put("mensaje", "Error al procesar el pago: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    // ── Historial ────────────────────────────────────────────
    @GetMapping("/historial/{idestudiante}")
    public ResponseEntity<?> historial(@PathVariable Integer idestudiante) {
        return ResponseEntity.ok(
            transaccionRepo.findByEstudianteIdestudianteOrderByFechaTransaccionDesc(idestudiante)
        );
    }

    // ── Validaciones ─────────────────────────────────────────
    private Map<String, Object> validarTarjeta(PasarelaRequest req) {
        Map<String, Object> r = new HashMap<>();
        String num = req.getNumeroTarjeta() == null ? ""
            : req.getNumeroTarjeta().replaceAll("[^0-9]", "");
        if (num.length() < 13) { r.put("valida",false); r.put("error","Número de tarjeta inválido"); return r; }
        String cvv = req.getCvv() == null ? "" : req.getCvv().replaceAll("[^0-9]","");
        if (cvv.length() < 3) { r.put("valida",false); r.put("error","CVV inválido"); return r; }
        if (req.getFechaVencimiento()==null || !req.getFechaVencimiento().matches("\\d{2}/\\d{2}"))
        { r.put("valida",false); r.put("error","Fecha de vencimiento inválida (MM/AA)"); return r; }
        r.put("valida", true); return r;
    }

    private Map<String, Object> validarYape(PasarelaRequest req) {
        Map<String, Object> r = new HashMap<>();
        String cel = req.getCelularYape() == null ? "" : req.getCelularYape().replaceAll("[^0-9]","");
        if (cel.length() != 9) { r.put("valida",false); r.put("error","Número de celular Yape inválido (9 dígitos)"); return r; }
        if (req.getCodigoYape()==null || req.getCodigoYape().trim().isEmpty())
        { r.put("valida",false); r.put("error","Código de aprobación Yape requerido"); return r; }
        r.put("valida", true); return r;
    }

    private Map<String, Object> validarPlin(PasarelaRequest req) {
        Map<String, Object> r = new HashMap<>();
        String cel = req.getCelularPlin()==null ? "" : req.getCelularPlin().replaceAll("[^0-9]","");
        if (cel.length() != 9) { r.put("valida",false); r.put("error","Número de celular Plin inválido (9 dígitos)"); return r; }
        if (req.getBancoPlin()==null || req.getBancoPlin().isEmpty())
        { r.put("valida",false); r.put("error","Selecciona tu banco para Plin"); return r; }
        r.put("valida", true); return r;
    }

    // ── Helpers ──────────────────────────────────────────────
    private boolean simularAprobacion(PasarelaRequest req, String metodo) {
        return switch (metodo) {
            case "YAPE" -> req.getCodigoYape() != null && !req.getCodigoYape().startsWith("0000");
            case "PLIN" -> req.getCelularPlin() != null && !req.getCelularPlin().startsWith("900");
            default -> {
                String num = req.getNumeroTarjeta()==null ? "" : req.getNumeroTarjeta().replaceAll("[^0-9]","");
                yield !num.startsWith("4000");
            }
        };
    }

    private String getTipoTarjeta(PasarelaRequest req, String metodo) {
        return switch (metodo) {
            case "YAPE" -> "YAPE";
            case "PLIN" -> "PLIN-" + (req.getBancoPlin()!=null ? req.getBancoPlin() : "");
            default -> {
                String n = req.getNumeroTarjeta()==null ? "" : req.getNumeroTarjeta().replaceAll("[^0-9]","");
                yield n.startsWith("4") ? "VISA" : n.startsWith("5") ? "MASTERCARD" : n.startsWith("3") ? "AMEX" : "OTRO";
            }
        };
    }

    private String getUltimosDigitos(PasarelaRequest req, String metodo) {
        return switch (metodo) {
            case "YAPE" -> req.getCelularYape()!=null && req.getCelularYape().length()>=4
                ? req.getCelularYape().substring(req.getCelularYape().length()-4) : "****";
            case "PLIN" -> req.getCelularPlin()!=null && req.getCelularPlin().length()>=4
                ? req.getCelularPlin().substring(req.getCelularPlin().length()-4) : "****";
            default -> {
                String n = req.getNumeroTarjeta()==null ? "" : req.getNumeroTarjeta().replaceAll("[^0-9]","");
                yield n.length()>=4 ? n.substring(n.length()-4) : "****";
            }
        };
    }

    private String getNombreTitular(PasarelaRequest req, String metodo) {
        return switch (metodo) {
            case "YAPE" -> "Yape: " + (req.getCelularYape()!=null ? req.getCelularYape() : "");
            case "PLIN" -> "Plin: " + (req.getCelularPlin()!=null ? req.getCelularPlin() : "");
            default -> (req.getNombreTitular()!=null ? req.getNombreTitular() : "") + " "
                + (req.getApellidoTitular()!=null ? req.getApellidoTitular() : "");
        };
    }

    private String getObservacion(boolean aprobado, String metodo) {
        if (!aprobado) return getMensajeRechazo(metodo);
        return switch (metodo) {
            case "YAPE" -> "Pago con Yape procesado exitosamente";
            case "PLIN" -> "Pago con Plin procesado exitosamente";
            default     -> "Pago con tarjeta procesado exitosamente";
        };
    }

    private String getMensajeRechazo(String metodo) {
        return switch (metodo) {
            case "YAPE" -> "Código Yape inválido o expirado. Verifica en tu app Yape.";
            case "PLIN" -> "Pago Plin rechazado. Verifica tu saldo o número de celular.";
            default     -> "Tarjeta rechazada. Verifica los datos o contacta a tu banco.";
        };
    }
}
