package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.MateriaCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.MateriaResponse;
import edu.upc.sistema.gestionacademicaapi.entity.Materia;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.exception.ReglaNegocioException;
import edu.upc.sistema.gestionacademicaapi.repository.MateriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MateriaService {

    private final MateriaRepository repository;
    private final CurrentUserService currentUser;

    @Transactional(readOnly = true)
    public List<MateriaResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
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
                .build();
    }
}
