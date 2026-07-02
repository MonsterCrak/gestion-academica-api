package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.CategoriaCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.CategoriaResponse;
import edu.upc.sistema.gestionacademicaapi.entity.CategoriaPolitica;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.repository.CategoriaPoliticaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoriaPoliticaService {

    private final CategoriaPoliticaRepository repository;
    private final CurrentUserService currentUser;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(UUID id) {
        return toResponse(buscarPorId(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaCreateRequest req) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        CategoriaPolitica c = CategoriaPolitica.builder()
                .nombreCategoria(req.getNombreCategoria())
                .maxItemsPorAlumno(req.getMaxItemsPorAlumno())
                .tiempoMaximoHoras(req.getTiempoMaximoHoras())
                .build();
        return toResponse(repository.save(c));
    }

    public CategoriaPolitica buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("CategoriaPolitica", id));
    }

    private CategoriaResponse toResponse(CategoriaPolitica c) {
        return CategoriaResponse.builder()
                .id(c.getId())
                .nombreCategoria(c.getNombreCategoria())
                .maxItemsPorAlumno(c.getMaxItemsPorAlumno())
                .tiempoMaximoHoras(c.getTiempoMaximoHoras())
                .build();
    }
}
