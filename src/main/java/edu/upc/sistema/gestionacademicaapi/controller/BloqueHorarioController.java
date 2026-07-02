package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.BloqueHorarioCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.BloqueHorarioResponse;
import edu.upc.sistema.gestionacademicaapi.service.BloqueHorarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bloqueos")
@RequiredArgsConstructor
public class BloqueHorarioController {

    private final BloqueHorarioService service;

    @GetMapping
    public List<BloqueHorarioResponse> listar() {
        return service.listar();
    }

    @PostMapping
    public BloqueHorarioResponse crear(@Valid @RequestBody BloqueHorarioCreateRequest req) {
        return service.crear(req);
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable UUID id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
