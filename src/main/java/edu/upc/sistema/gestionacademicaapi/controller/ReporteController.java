package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.ReporteCargaMensualResponse;
import edu.upc.sistema.gestionacademicaapi.service.CurrentUserService;
import edu.upc.sistema.gestionacademicaapi.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;
    private final CurrentUserService currentUser;

    @GetMapping("/carga-mensual")
    public ReporteCargaMensualResponse cargaMensual(
            @RequestParam Long docenteId,
            @RequestParam int anio,
            @RequestParam int mes) {
        return reporteService.cargaMensual(docenteId, anio, mes);
    }

    @GetMapping("/mi-carga-mensual")
    public ReporteCargaMensualResponse miCargaMensual(
            @RequestParam int anio,
            @RequestParam int mes) {
        Long miId = currentUser.obtenerActual().getId();
        return reporteService.cargaMensual(miId, anio, mes);
    }
}
