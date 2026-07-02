package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.AceptarInvitacionRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ApelarRequest;
import edu.upc.sistema.gestionacademicaapi.dto.AsignarDocenteRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ConfirmacionAlumnoResponse;
import edu.upc.sistema.gestionacademicaapi.dto.ConfirmarAsistenciaRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ResolverRevisionRequest;
import edu.upc.sistema.gestionacademicaapi.dto.SolicitudTutoriaCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.SolicitudTutoriaResponse;
import edu.upc.sistema.gestionacademicaapi.service.SolicitudTutoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tutorias")
@RequiredArgsConstructor
public class SolicitudTutoriaController {

    private final SolicitudTutoriaService service;

    @GetMapping
    public List<SolicitudTutoriaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public SolicitudTutoriaResponse obtener(@PathVariable UUID id) {
        return service.obtener(id);
    }

    @GetMapping("/{id}/confirmaciones")
    public List<ConfirmacionAlumnoResponse> listarConfirmaciones(@PathVariable UUID id) {
        return service.listarConfirmaciones(id);
    }

    @PostMapping
    public SolicitudTutoriaResponse crear(@Valid @RequestBody SolicitudTutoriaCreateRequest req) {
        return service.crear(req);
    }

    @PostMapping("/invitacion/{token}")
    public ConfirmacionAlumnoResponse aceptarORechazarInvitacion(
            @PathVariable String token,
            @Valid @RequestBody AceptarInvitacionRequest req) {
        return service.aceptarORechazarInvitacion(token, req.getAcepta());
    }

    @PostMapping("/{id}/asignar-docente")
    public SolicitudTutoriaResponse asignarDocente(
            @PathVariable UUID id,
            @Valid @RequestBody AsignarDocenteRequest req) {
        service.asignarDocente(id, req);
        return service.obtener(id);
    }

    @PostMapping("/{id}/confirmar-realizacion")
    public SolicitudTutoriaResponse confirmarRealizacion(@PathVariable UUID id) {
        return service.confirmarRealizacion(id);
    }

    @PostMapping("/{id}/asistencia")
    public ConfirmacionAlumnoResponse marcarAsistencia(
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmarAsistenciaRequest req) {
        return service.marcarAsistencia(id, req);
    }

    @PostMapping("/{id}/apelar")
    public ConfirmacionAlumnoResponse apelar(
            @PathVariable UUID id,
            @Valid @RequestBody ApelarRequest req) {
        return service.apelar(id, req);
    }

    @DeleteMapping("/{id}/apelar")
    public ConfirmacionAlumnoResponse retirarApelacion(@PathVariable UUID id) {
        return service.retirarApelacion(id);
    }

    @PostMapping("/{id}/resolver-revision")
    public SolicitudTutoriaResponse resolverRevision(
            @PathVariable UUID id,
            @Valid @RequestBody ResolverRevisionRequest req) {
        return service.resolverRevision(id, req);
    }
}
