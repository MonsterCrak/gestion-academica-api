package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.DeudaUpdateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.PenalizacionCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.PenalizacionResponse;
import edu.upc.sistema.gestionacademicaapi.service.PenalizacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/penalizaciones")
@RequiredArgsConstructor
public class PenalizacionController {

    private final PenalizacionService service;

    @GetMapping("/mias")
    public Page<PenalizacionResponse> misPenalizaciones(
            @PageableDefault(size = 20, sort = "fechaInicio", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.misPenalizaciones(pageable);
    }

    @GetMapping("/usuario/{id}")
    public Page<PenalizacionResponse> porUsuario(@PathVariable Long id,
            @PageableDefault(size = 20, sort = "fechaInicio", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listarDeUsuario(id, pageable);
    }

    @PostMapping
    public PenalizacionResponse aplicar(@Valid @RequestBody PenalizacionCreateRequest req) {
        return service.aplicarManual(req);
    }

    @PutMapping("/usuario/{id}/deuda")
    public void actualizarDeuda(@PathVariable Long id, @Valid @RequestBody DeudaUpdateRequest req) {
        service.actualizarDeuda(id, req);
    }
}
