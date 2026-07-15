package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.BloqueHorarioCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.BloqueHorarioResponse;
import edu.upc.sistema.gestionacademicaapi.service.BloqueHorarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bloqueos")
@RequiredArgsConstructor
public class BloqueHorarioController {

    private final BloqueHorarioService service;

    /** Sin filtros: todos los bloqueos activos. Con espacioFisicoId/recursoId: filtra por aula o recurso. */
    @GetMapping
    public Page<BloqueHorarioResponse> listar(
            @RequestParam(required = false) Long espacioFisicoId,
            @RequestParam(required = false) Long recursoId,
            Pageable pageable) {
        return service.buscar(espacioFisicoId, recursoId, pageable);
    }

    @GetMapping("/{id}")
    public BloqueHorarioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public BloqueHorarioResponse crear(@Valid @RequestBody BloqueHorarioCreateRequest req) {
        return service.crear(req);
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
