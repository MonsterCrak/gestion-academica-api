package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.RecursoCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.RecursoResponse;
import edu.upc.sistema.gestionacademicaapi.service.RecursoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recursos")
@RequiredArgsConstructor
public class RecursoController {

    private final RecursoService service;

    @GetMapping
    public List<RecursoResponse> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long espacioActualId) {
        if (categoriaId != null) return service.listarPorCategoria(categoriaId);
        if (espacioActualId != null) return service.listarPorEspacioActual(espacioActualId);
        return service.listar();
    }

    @GetMapping("/{id}")
    public RecursoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public RecursoResponse crear(@Valid @RequestBody RecursoCreateRequest req) {
        return service.crear(req);
    }
}
