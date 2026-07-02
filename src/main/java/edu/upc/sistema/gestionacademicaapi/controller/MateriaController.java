package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.MateriaCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.MateriaResponse;
import edu.upc.sistema.gestionacademicaapi.service.MateriaService;
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
@RequestMapping("/materias")
@RequiredArgsConstructor
public class MateriaController {

    private final MateriaService service;

    @GetMapping
    public List<MateriaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public MateriaResponse obtener(@PathVariable UUID id) {
        return service.obtener(id);
    }

    @PostMapping
    public MateriaResponse crear(@Valid @RequestBody MateriaCreateRequest req) {
        return service.crear(req);
    }
}
