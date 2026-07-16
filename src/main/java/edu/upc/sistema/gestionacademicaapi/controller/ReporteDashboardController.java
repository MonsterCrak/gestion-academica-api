package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.ReporteDashboardResponse;
import edu.upc.sistema.gestionacademicaapi.service.ReporteDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteDashboardController {

    private static final String EXCEL_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ReporteDashboardService service;

    @GetMapping("/dashboard")
    public ReporteDashboardResponse dashboard() {
        return service.dashboard();
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> excel() {
        byte[] cuerpo = service.exportarExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-operativo.xlsx\"")
                .contentType(MediaType.parseMediaType(EXCEL_MIME))
                .body(cuerpo);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> pdf() {
        byte[] cuerpo = service.exportarPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-operativo.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(cuerpo);
    }
}
