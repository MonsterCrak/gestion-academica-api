package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.EspacioFisicoCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.EspacioFisicoResponse;
import edu.upc.sistema.gestionacademicaapi.dto.EspacioFisicoUpdateRequest;
import edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio;
import edu.upc.sistema.gestionacademicaapi.service.EspacioFisicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/espacios")
@RequiredArgsConstructor
public class EspacioFisicoController {

    private final EspacioFisicoService service;

    @GetMapping
    public Page<EspacioFisicoResponse> listar(@RequestParam(required = false) TipoEspacio tipo, Pageable pageable) {
        return tipo == null ? service.listar(pageable) : service.listarPorTipo(tipo, pageable);
    }

    @GetMapping("/reservables")
    public Page<EspacioFisicoResponse> listarReservables(Pageable pageable) {
        return service.listarReservables(pageable);
    }

    @GetMapping("/{id}")
    public EspacioFisicoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public EspacioFisicoResponse crear(@Valid @RequestBody EspacioFisicoCreateRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    public EspacioFisicoResponse actualizar(@PathVariable Long id, @Valid @RequestBody EspacioFisicoUpdateRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public EspacioFisicoResponse darDeBaja(@PathVariable Long id) {
        return service.darDeBaja(id);
    }

    @PostMapping("/{id}/reactivar")
    public EspacioFisicoResponse reactivar(@PathVariable Long id) {
        return service.reactivar(id);
    }
}
