package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.DisponibilidadRecursoResponse;
import edu.upc.sistema.gestionacademicaapi.dto.RecursoCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.RecursoResponse;
import edu.upc.sistema.gestionacademicaapi.dto.RecursoUpdateRequest;
import edu.upc.sistema.gestionacademicaapi.entity.CategoriaPolitica;
import edu.upc.sistema.gestionacademicaapi.entity.EspacioFisico;
import edu.upc.sistema.gestionacademicaapi.entity.Recurso;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoRecurso;
import edu.upc.sistema.gestionacademicaapi.enums.TipoMovilidad;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.exception.ReglaNegocioException;
import edu.upc.sistema.gestionacademicaapi.repository.CategoriaPoliticaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.EspacioFisicoRepository;
import edu.upc.sistema.gestionacademicaapi.repository.RecursoRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Inventario de equipos (HU-06/07/08): CRUD, mantenimiento/baja, disponibilidad y búsqueda.
 */
@Service
@RequiredArgsConstructor
public class RecursoService {

    private final RecursoRepository repository;
    private final EspacioFisicoRepository espacioRepository;
    private final CategoriaPoliticaRepository categoriaRepository;
    private final CurrentUserService currentUser;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<RecursoResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RecursoResponse obtener(Long id) {
        return toResponse(buscarPorId(id));
    }

    /** HU-08: búsqueda y filtrado avanzado con paginación. */
    @Transactional(readOnly = true)
    public Page<RecursoResponse> buscar(String q, EstadoRecurso estado, Long categoriaId,
                                        Long espacioId, TipoMovilidad tipoMovilidad, Pageable pageable) {
        Specification<Recurso> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("nombre")), like),
                        cb.like(cb.lower(root.get("codigoInventario")), like),
                        cb.like(cb.lower(root.get("numeroSerie")), like)));
            }
            if (estado != null) ps.add(cb.equal(root.get("estado"), estado));
            if (categoriaId != null) ps.add(cb.equal(root.get("categoria").get("id"), categoriaId));
            if (espacioId != null) ps.add(cb.equal(root.get("espacioActual").get("id"), espacioId));
            if (tipoMovilidad != null) ps.add(cb.equal(root.get("tipoMovilidad"), tipoMovilidad));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return repository.findAll(spec, pageable).map(this::toResponse);
    }

    /** HU-07: resumen de disponibilidad en tiempo real. */
    @Transactional(readOnly = true)
    public DisponibilidadRecursoResponse disponibilidad() {
        return DisponibilidadRecursoResponse.builder()
                .total(repository.count())
                .disponibles(repository.countByEstado(EstadoRecurso.DISPONIBLE))
                .prestados(repository.countByEstado(EstadoRecurso.PRESTADO))
                .reservados(repository.countByEstado(EstadoRecurso.RESERVADO))
                .mantenimiento(repository.countByEstado(EstadoRecurso.MANTENIMIENTO))
                .dadosDeBaja(repository.countByEstado(EstadoRecurso.DADO_DE_BAJA))
                .build();
    }

    @Transactional
    public RecursoResponse crear(RecursoCreateRequest req) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        if (repository.existsByCodigoInventario(req.getCodigoInventario())) {
            throw new ReglaNegocioException("CODIGO_DUPLICADO",
                    "Ya existe un recurso con codigo " + req.getCodigoInventario());
        }
        if (tieneSerie(req.getNumeroSerie()) && repository.existsByNumeroSerieIgnoreCase(req.getNumeroSerie())) {
            throw new ReglaNegocioException("SERIE_DUPLICADA",
                    "Ya existe un equipo con numero de serie " + req.getNumeroSerie());
        }

        CategoriaPolitica categoria = categoriaRepository.findById(req.getCategoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria", req.getCategoriaId()));
        EspacioFisico espacio = resolverEspacio(req.getEspacioActualId());

        Recurso r = Recurso.builder()
                .categoria(categoria)
                .codigoInventario(req.getCodigoInventario())
                .numeroSerie(req.getNumeroSerie())
                .nombre(req.getNombre())
                .tipoMovilidad(req.getTipoMovilidad())
                .espacioActual(espacio)
                .estado(EstadoRecurso.DISPONIBLE)
                .requiereUbicacionFisica(req.getTipoMovilidad() == TipoMovilidad.FIJO_EN_AULA)
                .build();

        Recurso saved = repository.save(r);
        auditoriaService.registrar("CREA_RECURSO", "Recurso", String.valueOf(saved.getId()),
                AuditoriaService.OK, "Alta de recurso " + saved.getNombre());
        return toResponse(saved);
    }

    @Transactional
    public RecursoResponse actualizar(Long id, RecursoUpdateRequest req) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        Recurso r = buscarPorId(id);

        if (req.getNombre() != null) r.setNombre(req.getNombre());
        if (req.getCategoriaId() != null) {
            r.setCategoria(categoriaRepository.findById(req.getCategoriaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria", req.getCategoriaId())));
        }
        if (req.getNumeroSerie() != null) {
            if (tieneSerie(req.getNumeroSerie())
                    && repository.existsByNumeroSerieIgnoreCaseAndIdNot(req.getNumeroSerie(), id)) {
                throw new ReglaNegocioException("SERIE_DUPLICADA",
                        "Ya existe un equipo con numero de serie " + req.getNumeroSerie());
            }
            r.setNumeroSerie(req.getNumeroSerie());
        }
        if (req.getTipoMovilidad() != null) {
            r.setTipoMovilidad(req.getTipoMovilidad());
            r.setRequiereUbicacionFisica(req.getTipoMovilidad() == TipoMovilidad.FIJO_EN_AULA);
        }
        if (req.getEspacioActualId() != null) {
            r.setEspacioActual(resolverEspacio(req.getEspacioActualId()));
        }

        Recurso saved = repository.save(r);
        auditoriaService.registrar("EDITA_RECURSO", "Recurso", String.valueOf(id),
                AuditoriaService.OK, "Edicion de recurso " + saved.getNombre());
        return toResponse(saved);
    }

    @Transactional
    public RecursoResponse marcarMantenimiento(Long id) {
        return cambiarEstadoAdmin(id, EstadoRecurso.MANTENIMIENTO);
    }

    @Transactional
    public RecursoResponse marcarDisponible(Long id) {
        return cambiarEstadoAdmin(id, EstadoRecurso.DISPONIBLE);
    }

    @Transactional
    public RecursoResponse darDeBaja(Long id) {
        return cambiarEstadoAdmin(id, EstadoRecurso.DADO_DE_BAJA);
    }

    public Recurso buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Recurso", id));
    }

    // --- internos ---

    private RecursoResponse cambiarEstadoAdmin(Long id, EstadoRecurso nuevo) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        Recurso r = buscarPorId(id);
        if (r.getEstado() == EstadoRecurso.PRESTADO || r.getEstado() == EstadoRecurso.RESERVADO) {
            throw new ReglaNegocioException("EN_USO",
                    "No se puede cambiar el estado: el recurso esta " + r.getEstado());
        }
        r.setEstado(nuevo);
        Recurso saved = repository.save(r);
        auditoriaService.registrar("CAMBIA_ESTADO_RECURSO", "Recurso", String.valueOf(id),
                AuditoriaService.OK, "Nuevo estado " + nuevo);
        return toResponse(saved);
    }

    private EspacioFisico resolverEspacio(Long espacioId) {
        if (espacioId == null) return null;
        return espacioRepository.findById(espacioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("EspacioFisico", espacioId));
    }

    private boolean tieneSerie(String serie) {
        return serie != null && !serie.isBlank();
    }

    private RecursoResponse toResponse(Recurso r) {
        return RecursoResponse.builder()
                .id(r.getId())
                .categoriaId(r.getCategoria() != null ? r.getCategoria().getId() : null)
                .categoriaNombre(r.getCategoria() != null ? r.getCategoria().getNombreCategoria() : null)
                .numeroSerie(r.getNumeroSerie())
                .codigoInventario(r.getCodigoInventario())
                .nombre(r.getNombre())
                .tipoMovilidad(r.getTipoMovilidad())
                .espacioActualId(r.getEspacioActual() != null ? r.getEspacioActual().getId() : null)
                .espacioActualCodigo(r.getEspacioActual() != null ? r.getEspacioActual().getCodigo() : null)
                .estado(r.getEstado())
                .requiereUbicacionFisica(r.getRequiereUbicacionFisica())
                .build();
    }
}
