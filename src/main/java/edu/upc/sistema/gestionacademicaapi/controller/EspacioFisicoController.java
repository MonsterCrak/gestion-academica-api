package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.EspacioFisicoCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.EspacioFisicoResponse;
import edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio;
import edu.upc.sistema.gestionacademicaapi.service.EspacioFisicoService;
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
@RequestMapping("/espacios")
@RequiredArgsConstructor
public class EspacioFisicoController {

    private final EspacioFisicoService service;

    @GetMapping
    public List<EspacioFisicoResponse> listar(@RequestParam(required = false) TipoEspacio tipo) {
        return tipo == null ? service.listar() : service.listarPorTipo(tipo);
    }

    @GetMapping("/reservables")
    public List<EspacioFisicoResponse> listarReservables() {
        return service.listarReservables();
    }

    @GetMapping("/{id}")
    public EspacioFisicoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public EspacioFisicoResponse crear(@Valid @RequestBody EspacioFisicoCreateRequest req) {
        return service.crear(req);
    }
}
