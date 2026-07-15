package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.BloqueHorarioCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.BloqueHorarioResponse;
import edu.upc.sistema.gestionacademicaapi.entity.BloqueHorario;
import edu.upc.sistema.gestionacademicaapi.enums.TipoBloqueo;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.exception.ReglaNegocioException;
import edu.upc.sistema.gestionacademicaapi.repository.BloqueHorarioRepository;
import edu.upc.sistema.gestionacademicaapi.repository.EspacioFisicoRepository;
import edu.upc.sistema.gestionacademicaapi.repository.RecursoRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BloqueHorarioService {

    private final BloqueHorarioRepository repository;
    private final EspacioFisicoRepository espacioRepository;
    private final RecursoRepository recursoRepository;
    private final CurrentUserService currentUser;

    /** Filtro por aula (espacioFisicoId) y/o recurso (recursoId), solo bloqueos activos. Uso administrativo. */
    @Transactional(readOnly = true)
    public Page<BloqueHorarioResponse> buscar(Long espacioFisicoId, Long recursoId, Pageable pageable) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        Specification<BloqueHorario> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.isTrue(root.get("activo")));
            if (espacioFisicoId != null) ps.add(cb.equal(root.get("espacioFisico").get("id"), espacioFisicoId));
            if (recursoId != null) ps.add(cb.equal(root.get("recurso").get("id"), recursoId));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return repository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public BloqueHorarioResponse obtener(Long id) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        return toResponse(buscarPorId(id));
    }

    @Transactional
    public BloqueHorarioResponse crear(BloqueHorarioCreateRequest req) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);

        if (!req.getHoraInicio().isBefore(req.getHoraFin())) {
            throw new ReglaNegocioException("HORARIO_INVALIDO", "horaInicio debe ser anterior a horaFin");
        }
        if (req.getFechaHasta() != null && req.getFechaHasta().isBefore(req.getFechaDesde())) {
            throw new ReglaNegocioException("HORARIO_INVALIDO", "fechaHasta debe ser >= fechaDesde");
        }

        BloqueHorario bloque = new BloqueHorario();
        bloque.setTipoBloqueo(req.getTipoBloqueo());
        bloque.setFechaDesde(req.getFechaDesde());
        bloque.setFechaHasta(req.getFechaHasta());
        bloque.setHoraInicio(req.getHoraInicio());
        bloque.setHoraFin(req.getHoraFin());
        bloque.setMotivo(req.getMotivo());
        bloque.setActivo(true);

        if (req.getTipoBloqueo() == TipoBloqueo.DISPONIBILIDAD_AULA) {
            if (req.getEspacioFisicoId() == null) {
                throw new ReglaNegocioException("BLOQUEO_INVALIDO", "espacioFisicoId requerido para DISPONIBILIDAD_AULA");
            }
            bloque.setEspacioFisico(espacioRepository.findById(req.getEspacioFisicoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("EspacioFisico", req.getEspacioFisicoId())));
        } else if (req.getTipoBloqueo() == TipoBloqueo.FUERA_ATENCION_EQUIPOS) {
            if (req.getRecursoId() == null) {
                throw new ReglaNegocioException("BLOQUEO_INVALIDO", "recursoId requerido para FUERA_ATENCION_EQUIPOS");
            }
            bloque.setRecurso(recursoRepository.findById(req.getRecursoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Recurso", req.getRecursoId())));
        }

        return toResponse(repository.save(bloque));
    }

    @Transactional
    public void desactivar(Long id) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        BloqueHorario b = buscarPorId(id);
        b.setActivo(false);
        repository.save(b);
    }

    private BloqueHorario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("BloqueHorario", id));
    }

    public boolean solapa(BloqueHorario bloque, java.time.LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        if (!fecha.isBefore(bloque.getFechaDesde())) {
            return false;
        }
        if (bloque.getFechaHasta() != null && fecha.isAfter(bloque.getFechaHasta())) {
            return false;
        }
        return !(horaFin.compareTo(bloque.getHoraInicio()) <= 0
                || horaInicio.compareTo(bloque.getHoraFin()) >= 0);
    }

    private BloqueHorarioResponse toResponse(BloqueHorario b) {
        return BloqueHorarioResponse.builder()
                .id(b.getId())
                .tipoBloqueo(b.getTipoBloqueo())
                .espacioFisicoId(b.getEspacioFisico() != null ? b.getEspacioFisico().getId() : null)
                .recursoId(b.getRecurso() != null ? b.getRecurso().getId() : null)
                .fechaDesde(b.getFechaDesde())
                .fechaHasta(b.getFechaHasta())
                .horaInicio(b.getHoraInicio())
                .horaFin(b.getHoraFin())
                .motivo(b.getMotivo())
                .activo(b.getActivo())
                .build();
    }
}
