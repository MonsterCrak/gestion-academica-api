package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.DisponibilidadRecursoResponse;
import edu.upc.sistema.gestionacademicaapi.dto.RecursoCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.RecursoResponse;
import edu.upc.sistema.gestionacademicaapi.dto.RecursoUpdateRequest;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoRecurso;
import edu.upc.sistema.gestionacademicaapi.enums.TipoMovilidad;
import edu.upc.sistema.gestionacademicaapi.service.RecursoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recursos")
@RequiredArgsConstructor
public class RecursoController {

    private final RecursoService service;

    /** HU-07/08: catálogo con búsqueda, filtros y paginación. nombreLike filtra solo por nombre (distinto de q, que busca en varios campos). */
    @GetMapping
    public Page<RecursoResponse> buscar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EstadoRecurso estado,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long espacioActualId,
            @RequestParam(required = false) TipoMovilidad tipoMovilidad,
            @RequestParam(required = false) String nombreLike,
            @PageableDefault(size = 12, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        return service.buscar(q, estado, categoriaId, espacioActualId, tipoMovilidad, nombreLike, pageable);
    }

    @GetMapping("/disponibilidad")
    public DisponibilidadRecursoResponse disponibilidad() {
        return service.disponibilidad();
    }

    @GetMapping("/{id}")
    public RecursoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public RecursoResponse crear(@Valid @RequestBody RecursoCreateRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    public RecursoResponse actualizar(@PathVariable Long id, @Valid @RequestBody RecursoUpdateRequest req) {
        return service.actualizar(id, req);
    }

    @PostMapping("/{id}/mantenimiento")
    public RecursoResponse marcarMantenimiento(@PathVariable Long id) {
        return service.marcarMantenimiento(id);
    }

    @PostMapping("/{id}/disponible")
    public RecursoResponse marcarDisponible(@PathVariable Long id) {
        return service.marcarDisponible(id);
    }

    @DeleteMapping("/{id}")
    public RecursoResponse darDeBaja(@PathVariable Long id) {
        return service.darDeBaja(id);
    }
}
