package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.DocenteMateriaCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.DocenteMateriaResponse;
import edu.upc.sistema.gestionacademicaapi.service.DocenteMateriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/docente-materias")
@RequiredArgsConstructor
public class DocenteMateriaController {

    private final DocenteMateriaService service;

    @GetMapping("/mias")
    public List<DocenteMateriaResponse> misPreferencias() {
        return service.listarMias();
    }

    @GetMapping
    public List<DocenteMateriaResponse> listar(@RequestParam Long docenteId) {
        return service.listarPorDocente(docenteId);
    }

    @PostMapping
    public DocenteMateriaResponse registrar(@Valid @RequestBody DocenteMateriaCreateRequest req) {
        return service.registrar(req);
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
