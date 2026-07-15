package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.AceptarInvitacionRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ApelarRequest;
import edu.upc.sistema.gestionacademicaapi.dto.AsignarDocenteRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ConfirmacionAlumnoResponse;
import edu.upc.sistema.gestionacademicaapi.dto.ConfirmarAsistenciaRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ResolverRevisionRequest;
import edu.upc.sistema.gestionacademicaapi.dto.SolicitudTutoriaCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.SolicitudTutoriaDetalleResponse;
import edu.upc.sistema.gestionacademicaapi.dto.SolicitudTutoriaResponse;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoSolicitud;
import edu.upc.sistema.gestionacademicaapi.service.SolicitudTutoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/tutorias-legacy")
@RequiredArgsConstructor
public class SolicitudTutoriaController {

    private final SolicitudTutoriaService service;

    /** Busqueda avanzada: estado, materiaId, docenteId, fechaDesde, fechaHasta, creadorId. */
    @GetMapping
    public Page<SolicitudTutoriaResponse> listar(
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(required = false) Long materiaId,
            @RequestParam(required = false) Long docenteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Long creadorId,
            Pageable pageable) {
        return service.listar(estado, materiaId, docenteId, fechaDesde, fechaHasta, creadorId, pageable);
    }

    /** Solicitudes creadas por el estudiante autenticado. */
    @GetMapping("/mias")
    public Page<SolicitudTutoriaResponse> misSolicitudes(Pageable pageable) {
        return service.misSolicitudes(pageable);
    }

    /** Solicitudes donde el docente autenticado es el docenteAsignado. */
    @GetMapping("/asignadas-a-mi")
    public Page<SolicitudTutoriaResponse> asignadasAMi(Pageable pageable) {
        return service.asignadasAMi(pageable);
    }

    /** Enriquecido: incluye la lista de confirmados con su estado (evita la llamada aparte a /confirmaciones). */
    @GetMapping("/{id}")
    public SolicitudTutoriaDetalleResponse obtener(@PathVariable Long id) {
        return service.obtenerDetalle(id);
    }

    @GetMapping("/{id}/confirmaciones")
    public Page<ConfirmacionAlumnoResponse> listarConfirmaciones(@PathVariable Long id, Pageable pageable) {
        return service.listarConfirmaciones(id, pageable);
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

    /** El creador puede cancelar solo mientras la solicitud sigue PENDIENTE_QUORUM. */
    @PostMapping("/{id}/cancelar")
    public SolicitudTutoriaResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    @PostMapping("/{id}/asignar-docente")
    public SolicitudTutoriaResponse asignarDocente(
            @PathVariable Long id,
            @Valid @RequestBody AsignarDocenteRequest req) {
        service.asignarDocente(id, req);
        return service.obtener(id);
    }

    @PostMapping("/{id}/confirmar-realizacion")
    public SolicitudTutoriaResponse confirmarRealizacion(@PathVariable Long id) {
        return service.confirmarRealizacion(id);
    }

    @PostMapping("/{id}/asistencia")
    public ConfirmacionAlumnoResponse marcarAsistencia(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmarAsistenciaRequest req) {
        return service.marcarAsistencia(id, req);
    }

    @PostMapping("/{id}/apelar")
    public ConfirmacionAlumnoResponse apelar(
            @PathVariable Long id,
            @Valid @RequestBody ApelarRequest req) {
        return service.apelar(id, req);
    }

    @DeleteMapping("/{id}/apelar")
    public ConfirmacionAlumnoResponse retirarApelacion(@PathVariable Long id) {
        return service.retirarApelacion(id);
    }

    @PostMapping("/{id}/resolver-revision")
    public SolicitudTutoriaResponse resolverRevision(
            @PathVariable Long id,
            @Valid @RequestBody ResolverRevisionRequest req) {
        return service.resolverRevision(id, req);
    }
}
