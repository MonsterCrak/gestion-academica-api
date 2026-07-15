package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.MateriaCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.MateriaResponse;
import edu.upc.sistema.gestionacademicaapi.dto.MateriaUpdateRequest;
import edu.upc.sistema.gestionacademicaapi.service.MateriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/materias")
@RequiredArgsConstructor
public class MateriaController {

    private final MateriaService service;

    /** Busqueda avanzada: departamento, nombreLike. */
    @GetMapping
    public Page<MateriaResponse> listar(
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String nombreLike,
            Pageable pageable) {
        return service.listar(departamento, nombreLike, pageable);
    }

    @GetMapping("/{id}")
    public MateriaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public MateriaResponse crear(@Valid @RequestBody MateriaCreateRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    public MateriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody MateriaUpdateRequest req) {
        return service.actualizar(id, req);
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
