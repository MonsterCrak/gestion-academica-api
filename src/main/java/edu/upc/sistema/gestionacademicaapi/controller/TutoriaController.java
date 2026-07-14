package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.AsignarSesionRequest;
import edu.upc.sistema.gestionacademicaapi.dto.DemandaMateriaResponse;
import edu.upc.sistema.gestionacademicaapi.dto.InscribirTutoriaRequest;
import edu.upc.sistema.gestionacademicaapi.dto.InscripcionTutoriaResponse;
import edu.upc.sistema.gestionacademicaapi.dto.SesionTutoriaResponse;
import edu.upc.sistema.gestionacademicaapi.service.TutoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tutorias")
@RequiredArgsConstructor
public class TutoriaController {

    private final TutoriaService service;

    /** HU-16: tablero de demanda por asignatura con el progreso del quórum. */
    @GetMapping("/demanda")
    public List<DemandaMateriaResponse> demanda() {
        return service.listarDemanda();
    }

    @PostMapping("/inscribir")
    public DemandaMateriaResponse inscribir(@Valid @RequestBody InscribirTutoriaRequest req) {
        return service.inscribir(req);
    }

    @GetMapping("/mis-inscripciones")
    public List<InscripcionTutoriaResponse> misInscripciones() {
        return service.misInscripciones();
    }

    @GetMapping("/sesiones")
    public List<SesionTutoriaResponse> sesiones() {
        return service.listarSesiones();
    }

    @PostMapping("/sesiones/{id}/asignar")
    public SesionTutoriaResponse asignar(@PathVariable Long id, @Valid @RequestBody AsignarSesionRequest req) {
        return service.asignarRecursos(id, req);
    }
}
