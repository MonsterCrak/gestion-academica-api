package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.EspacioFisicoCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.EspacioFisicoResponse;
import edu.upc.sistema.gestionacademicaapi.dto.EspacioFisicoUpdateRequest;
import edu.upc.sistema.gestionacademicaapi.entity.EspacioFisico;
import edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.exception.ReglaNegocioException;
import edu.upc.sistema.gestionacademicaapi.repository.EspacioFisicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestión de aulas y espacios físicos (parte de HU-06).
 */
@Service
@RequiredArgsConstructor
public class EspacioFisicoService {

    private final EspacioFisicoRepository repository;
    private final CurrentUserService currentUser;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<EspacioFisicoResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EspacioFisicoResponse> listarPorTipo(TipoEspacio tipo) {
        return repository.findByTipoEspacio(tipo).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EspacioFisicoResponse> listarReservables() {
        return repository.findByPermitirReservaCompletaTrue().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EspacioFisicoResponse obtener(Long id) {
        return toResponse(buscarPorId(id));
    }

    @Transactional
    public EspacioFisicoResponse crear(EspacioFisicoCreateRequest req) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        if (repository.findByCodigo(req.getCodigo()).isPresent()) {
            throw new ReglaNegocioException("CODIGO_DUPLICADO",
                    "Ya existe un espacio con codigo " + req.getCodigo());
        }
        EspacioFisico e = EspacioFisico.builder()
                .codigo(req.getCodigo())
                .tipoEspacio(req.getTipoEspacio())
                .aforo(req.getAforo())
                .permitirPrestamoIndividual(req.getPermitirPrestamoIndividual())
                .permitirReservaCompleta(req.getPermitirReservaCompleta())
                .activo(true)
                .build();
        EspacioFisico saved = repository.save(e);
        auditoriaService.registrar("CREA_ESPACIO", "EspacioFisico", String.valueOf(saved.getId()),
                AuditoriaService.OK, "Alta de espacio " + saved.getCodigo());
        return toResponse(saved);
    }

    @Transactional
    public EspacioFisicoResponse actualizar(Long id, EspacioFisicoUpdateRequest req) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        EspacioFisico e = buscarPorId(id);
        if (req.getTipoEspacio() != null) e.setTipoEspacio(req.getTipoEspacio());
        if (req.getAforo() != null) e.setAforo(req.getAforo());
        if (req.getPermitirPrestamoIndividual() != null) e.setPermitirPrestamoIndividual(req.getPermitirPrestamoIndividual());
        if (req.getPermitirReservaCompleta() != null) e.setPermitirReservaCompleta(req.getPermitirReservaCompleta());
        EspacioFisico saved = repository.save(e);
        auditoriaService.registrar("EDITA_ESPACIO", "EspacioFisico", String.valueOf(id),
                AuditoriaService.OK, "Edicion de espacio " + saved.getCodigo());
        return toResponse(saved);
    }

    @Transactional
    public EspacioFisicoResponse darDeBaja(Long id) {
        return cambiarActivo(id, false);
    }

    @Transactional
    public EspacioFisicoResponse reactivar(Long id) {
        return cambiarActivo(id, true);
    }

    public EspacioFisico buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("EspacioFisico", id));
    }

    private EspacioFisicoResponse cambiarActivo(Long id, boolean activo) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        EspacioFisico e = buscarPorId(id);
        e.setActivo(activo);
        EspacioFisico saved = repository.save(e);
        auditoriaService.registrar(activo ? "REACTIVA_ESPACIO" : "BAJA_ESPACIO", "EspacioFisico",
                String.valueOf(id), AuditoriaService.OK, "Espacio " + saved.getCodigo() + (activo ? " reactivado" : " dado de baja"));
        return toResponse(saved);
    }

    private EspacioFisicoResponse toResponse(EspacioFisico e) {
        return EspacioFisicoResponse.builder()
                .id(e.getId())
                .codigo(e.getCodigo())
                .tipoEspacio(e.getTipoEspacio())
                .aforo(e.getAforo())
                .permitirPrestamoIndividual(e.getPermitirPrestamoIndividual())
                .permitirReservaCompleta(e.getPermitirReservaCompleta())
                .activo(e.getActivo())
                .build();
    }
}
