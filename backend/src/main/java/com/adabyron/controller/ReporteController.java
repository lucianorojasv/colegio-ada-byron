package com.adabyron.controller;

import com.adabyron.entity.Matricula;
import com.adabyron.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final MatriculaRepository matriculaRepo;

    @GetMapping("/matriculas/excel")
    public ResponseEntity<byte[]> exportarMatriculas(
            @RequestParam(defaultValue = "2026") String anio) throws Exception {

        List<Matricula> matriculas = matriculaRepo.findByAniolectivo(anio);

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("Matrículas " + anio);

            // ── Estilos ──────────────────────────────────────────
            XSSFCellStyle headerStyle = wb.createCellStyle();
            XSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)102,(byte)45,(byte)145}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // ── Título ───────────────────────────────────────────
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("COLEGIO ADA BYRON - Reporte de Matrículas " + anio);
            XSSFCellStyle titleStyle = wb.createCellStyle();
            XSSFFont titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short)14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0,0,0,6));

            // ── Encabezados ──────────────────────────────────────
            String[] headers = {"#","Alumno","DNI","Grado","Sección","Turno","Monto","Estado"};
            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // ── Datos ────────────────────────────────────────────
            int rowNum = 3;
            for (Matricula m : matriculas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(m.getIdmatricula());
                row.createCell(1).setCellValue(
                    m.getEstudiante().getPaterno() + " " +
                    m.getEstudiante().getMaterno() + ", " +
                    m.getEstudiante().getNombre());
                row.createCell(2).setCellValue(
                    m.getEstudiante().getDocidentidad() != null ? m.getEstudiante().getDocidentidad() : "");
                row.createCell(3).setCellValue(m.getSeccion().getGrado().getNombre());
                row.createCell(4).setCellValue(m.getSeccion().getNombre());
                row.createCell(5).setCellValue(m.getSeccion().getTurno());
                row.createCell(6).setCellValue(
                    m.getPago() != null ? m.getPago().getImporte().doubleValue() : 0);
                row.createCell(7).setCellValue(m.getCodestado());
            }

            // Autosize columns
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            wb.write(out);
            byte[] bytes = out.toByteArray();

            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            respHeaders.setContentDispositionFormData("attachment",
                "matriculas_" + anio + ".xlsx");

            return ResponseEntity.ok().headers(respHeaders).body(bytes);
        }
    }
}
