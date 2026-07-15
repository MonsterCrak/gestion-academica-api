package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.MateriaCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.MateriaResponse;
import edu.upc.sistema.gestionacademicaapi.dto.MateriaUpdateRequest;
import edu.upc.sistema.gestionacademicaapi.entity.Materia;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.exception.ReglaNegocioException;
import edu.upc.sistema.gestionacademicaapi.repository.MateriaRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MateriaService {

    private final MateriaRepository repository;
    private final CurrentUserService currentUser;

    /** Busqueda avanzada: filtra por departamento (exacto) y nombreLike (contiene, case-insensitive). */
    @Transactional(readOnly = true)
    public Page<MateriaResponse> listar(String departamento, String nombreLike, Pageable pageable) {
        Specification<Materia> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (departamento != null && !departamento.isBlank()) {
                ps.add(cb.equal(root.get("departamento"), departamento));
            }
            if (nombreLike != null && !nombreLike.isBlank()) {
                ps.add(cb.like(cb.lower(root.get("nombre")), "%" + nombreLike.toLowerCase() + "%"));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return repository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MateriaResponse obtener(Long id) {
        return toResponse(buscarPorId(id));
    }

    @Transactional
    public MateriaResponse crear(MateriaCreateRequest req) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        if (repository.existsByCodigo(req.getCodigo())) {
            throw new ReglaNegocioException("REPETIDO", "Ya existe una materia con codigo " + req.getCodigo());
        }
        Materia m = Materia.builder()
                .codigo(req.getCodigo())
                .nombre(req.getNombre())
                .departamento(req.getDepartamento())
                .build();
        return toResponse(repository.save(m));
    }

    @Transactional
    public MateriaResponse actualizar(Long id, MateriaUpdateRequest req) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        Materia m = buscarPorId(id);
        if (req.getNombre() != null) m.setNombre(req.getNombre());
        if (req.getDepartamento() != null) m.setDepartamento(req.getDepartamento());
        return toResponse(repository.save(m));
    }

    @Transactional
    public void desactivar(Long id) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        Materia m = buscarPorId(id);
        m.setActivo(false);
        repository.save(m);
    }

    public Materia buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Materia", id));
    }

    private MateriaResponse toResponse(Materia m) {
        return MateriaResponse.builder()
                .id(m.getId())
                .codigo(m.getCodigo())
                .nombre(m.getNombre())
                .departamento(m.getDepartamento())
                .activo(m.getActivo())
                .build();
    }
}
