package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.DevolucionRequest;
import edu.upc.sistema.gestionacademicaapi.dto.PrestamoCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.PrestamoResponse;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoPrestamo;
import edu.upc.sistema.gestionacademicaapi.enums.ModalidadPrestamo;
import edu.upc.sistema.gestionacademicaapi.service.PrestamoIndividualService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

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
    public Page<PrestamoResponse> misPrestamos(
            @PageableDefault(size = 20, sort = "fechaInicio", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listarMios(pageable);
    }

    @GetMapping("/activos")
    public Page<PrestamoResponse> activos(
            @PageableDefault(size = 20, sort = "fechaFin", direction = Sort.Direction.ASC) Pageable pageable) {
        return service.listarActivos(pageable);
    }

    /** Listado paginado con filtros (vista administrativa): estado, recursoId, fechaDesde, fechaHasta. */
    @GetMapping
    public Page<PrestamoResponse> listar(
            @RequestParam(required = false) EstadoPrestamo estado,
            @RequestParam(required = false) Long usuarioSolicitanteId,
            @RequestParam(required = false) Long recursoId,
            @RequestParam(required = false) ModalidadPrestamo modalidad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @PageableDefault(size = 20, sort = "fechaInicio", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.buscar(estado, usuarioSolicitanteId, recursoId, modalidad, fechaDesde, fechaHasta, pageable);
    }

    /** Detalle de un prestamo: el estudiante dueno o un administrativo. */
    @GetMapping("/{id}")
    public PrestamoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping("/{id}/devolver")
    public PrestamoResponse devolver(@PathVariable Long id,
                                     @RequestBody(required = false) DevolucionRequest req) {
        return service.devolver(id, req);
    }

    /** El estudiante extiende la fecha fin de su prestamo activo si la categoria del recurso lo permite. */
    @PostMapping("/{id}/extender")
    public PrestamoResponse extender(@PathVariable Long id) {
        return service.extender(id);
    }
}
