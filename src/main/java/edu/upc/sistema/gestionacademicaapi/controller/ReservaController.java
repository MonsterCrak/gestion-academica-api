package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.AvalRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ReservaCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ReservaResponse;
import edu.upc.sistema.gestionacademicaapi.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService service;

    @PostMapping
    public ReservaResponse solicitar(@Valid @RequestBody ReservaCreateRequest req) {
        return service.solicitar(req);
    }

    @GetMapping("/mias")
    public Page<ReservaResponse> misReservas(Pageable pageable) {
        return service.misReservas(pageable);
    }

    @GetMapping("/bandeja")
    public Page<ReservaResponse> bandeja(Pageable pageable) {
        return service.bandejaDocente(pageable);
    }

    @GetMapping("/aprobadas")
    public Page<ReservaResponse> aprobadas(Pageable pageable) {
        return service.aprobadas(pageable);
    }

    @PostMapping("/{id}/aval")
    public ReservaResponse avalar(@PathVariable Long id, @Valid @RequestBody AvalRequest req) {
        return service.avalar(id, req);
    }

    @PostMapping("/{id}/cancelar")
    public ReservaResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}
