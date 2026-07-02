package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.CategoriaCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.CategoriaResponse;
import edu.upc.sistema.gestionacademicaapi.service.CategoriaPoliticaService;
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
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaPoliticaService service;

    @GetMapping
    public List<CategoriaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CategoriaResponse obtener(@PathVariable UUID id) {
        return service.obtener(id);
    }

    @PostMapping
    public CategoriaResponse crear(@Valid @RequestBody CategoriaCreateRequest req) {
        return service.crear(req);
    }
}
