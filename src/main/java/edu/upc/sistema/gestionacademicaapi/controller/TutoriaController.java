package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.AsignarSesionRequest;
import edu.upc.sistema.gestionacademicaapi.dto.CrearSesionTutoriaRequest;
import edu.upc.sistema.gestionacademicaapi.dto.DemandaMateriaResponse;
import edu.upc.sistema.gestionacademicaapi.dto.InscribirTutoriaRequest;
import edu.upc.sistema.gestionacademicaapi.dto.InscripcionTutoriaResponse;
import edu.upc.sistema.gestionacademicaapi.dto.InscritoTutoriaResponse;
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

    /** Un docente o administrador crea una sesión con cupo, abierta a inscripción libre de alumnos. */
    @PostMapping("/sesiones")
    public SesionTutoriaResponse crearSesion(@Valid @RequestBody CrearSesionTutoriaRequest req) {
        return service.crearSesion(req);
    }

    /** Un alumno se inscribe en una sesión abierta mientras haya cupo. */
    @PostMapping("/sesiones/{id}/inscribir")
    public SesionTutoriaResponse inscribirEnSesion(@PathVariable Long id) {
        return service.inscribirEnSesion(id);
    }

    /** El alumno cancela su inscripción pendiente en la demanda de una asignatura. */
    @PostMapping("/demanda/{materiaId}/cancelar")
    public void cancelarDemanda(@PathVariable Long materiaId) {
        service.cancelarDemanda(materiaId);
    }

    /** El alumno se retira de una sesión abierta antes de que se realice. */
    @PostMapping("/sesiones/{id}/cancelar-inscripcion")
    public void cancelarInscripcion(@PathVariable Long id) {
        service.cancelarInscripcionSesion(id);
    }

    /** El administrador da de baja una sesión de tutoría. */
    @PostMapping("/sesiones/{id}/baja")
    public SesionTutoriaResponse darDeBaja(@PathVariable Long id) {
        return service.cancelarSesion(id);
    }

    /** Lista los alumnos inscritos en una sesión (solo el docente a cargo o un administrador). */
    @GetMapping("/sesiones/{id}/inscritos")
    public List<InscritoTutoriaResponse> inscritos(@PathVariable Long id) {
        return service.listarInscritos(id);
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
