package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.PrestamoCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.PrestamoResponse;
import edu.upc.sistema.gestionacademicaapi.service.PrestamoIndividualService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/prestamos")
@RequiredArgsConstructor
public class PrestamoIndividualController {

    private final PrestamoIndividualService service;

    @PostMapping
    public PrestamoResponse solicitar(@Valid @RequestBody PrestamoCreateRequest req) {
        return service.solicitar(req);
    }

    @GetMapping("/mios")
    public List<PrestamoResponse> misPrestamos() {
        return service.listarMios();
    }

    @PostMapping("/{id}/devolver")
    public PrestamoResponse devolver(@PathVariable UUID id) {
        return service.devolver(id);
    }
}
