package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.DevolucionRequest;
import edu.upc.sistema.gestionacademicaapi.dto.PrestamoCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.PrestamoResponse;
import edu.upc.sistema.gestionacademicaapi.service.PrestamoIndividualService;
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
@RequestMapping("/prestamos")
@RequiredArgsConstructor
public class PrestamoIndividualController {

    private final PrestamoIndividualService service;

    @PostMapping
    public PrestamoResponse solicitar(@Valid @RequestBody PrestamoCreateRequest req) {
        return service.solicitar(req);
    }

    @GetMapping("/mios")
    public List<PrestamoResponse> misPrestamos() {
        return service.listarMios();
    }

    @GetMapping("/activos")
    public List<PrestamoResponse> activos() {
        return service.listarActivos();
    }

    @GetMapping("/pendientes")
    public List<PrestamoResponse> pendientes() {
        return service.listarPendientes();
    }

    /** El administrador aprueba una solicitud pendiente (pasa a ACTIVO y se retira el equipo). */
    @PostMapping("/{id}/aprobar")
    public PrestamoResponse aprobar(@PathVariable Long id) {
        return service.aprobar(id);
    }

    @PostMapping("/{id}/devolver")
    public PrestamoResponse devolver(@PathVariable Long id,
                                     @RequestBody(required = false) DevolucionRequest req) {
        return service.devolver(id, req);
    }

    /** El alumno cancela su préstamo activo; el administrador puede darlo de baja. */
    @PostMapping("/{id}/cancelar")
    public PrestamoResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}
