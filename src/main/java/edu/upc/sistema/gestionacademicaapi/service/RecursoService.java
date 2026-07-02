package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.RecursoCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.RecursoResponse;
import edu.upc.sistema.gestionacademicaapi.entity.EspacioFisico;
import edu.upc.sistema.gestionacademicaapi.entity.Recurso;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoRecurso;
import edu.upc.sistema.gestionacademicaapi.enums.TipoMovilidad;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.exception.ReglaNegocioException;
import edu.upc.sistema.gestionacademicaapi.repository.EspacioFisicoRepository;
import edu.upc.sistema.gestionacademicaapi.repository.RecursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecursoService {

    private final RecursoRepository repository;
    private final EspacioFisicoRepository espacioRepository;
    private final CurrentUserService currentUser;

    @Transactional(readOnly = true)
    public List<RecursoResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RecursoResponse> listarPorCategoria(Long categoriaId) {
        return repository.findByCategoria_Id(categoriaId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RecursoResponse> listarPorEspacioActual(Long espacioId) {
        return repository.findByEspacioActual_Id(espacioId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RecursoResponse obtener(Long id) {
        return toResponse(buscarPorId(id));
    }

    @Transactional
    public RecursoResponse crear(RecursoCreateRequest req) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        if (repository.findByCodigoInventario(req.getCodigoInventario()).isPresent()) {
            throw new ReglaNegocioException("REPETIDO", "Ya existe un recurso con codigo " + req.getCodigoInventario());
        }
        EspacioFisico espacio = req.getEspacioActualId() == null
                ? null
                : espacioRepository.findById(req.getEspacioActualId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("EspacioFisico", req.getEspacioActualId()));

        Recurso r = Recurso.builder()
                .codigoInventario(req.getCodigoInventario())
                .numeroSerie(req.getNumeroSerie())
                .nombre(req.getNombre())
                .tipoMovilidad(req.getTipoMovilidad())
                .espacioActual(espacio)
                .estado(EstadoRecurso.DISPONIBLE)
                .requiereUbicacionFisica(req.getTipoMovilidad() == TipoMovilidad.FIJO_EN_AULA)
                .build();
        return toResponse(repository.save(r));
    }

    public Recurso buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Recurso", id));
    }

    private RecursoResponse toResponse(Recurso r) {
        return RecursoResponse.builder()
                .id(r.getId())
                .categoriaId(r.getCategoria() != null ? r.getCategoria().getId() : null)
                .numeroSerie(r.getNumeroSerie())
                .codigoInventario(r.getCodigoInventario())
                .nombre(r.getNombre())
                .tipoMovilidad(r.getTipoMovilidad())
                .espacioActualId(r.getEspacioActual() != null ? r.getEspacioActual().getId() : null)
                .estado(r.getEstado())
                .requiereUbicacionFisica(r.getRequiereUbicacionFisica())
                .build();
    }
}
