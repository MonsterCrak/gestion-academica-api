package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.CategoriaCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.CategoriaResponse;
import edu.upc.sistema.gestionacademicaapi.dto.CategoriaUpdateRequest;
import edu.upc.sistema.gestionacademicaapi.entity.CategoriaPolitica;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.repository.CategoriaPoliticaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaPoliticaService {

    private final CategoriaPoliticaRepository repository;
    private final CurrentUserService currentUser;

    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        return toResponse(buscarPorId(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaCreateRequest req) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        CategoriaPolitica c = CategoriaPolitica.builder()
                .nombreCategoria(req.getNombreCategoria())
                .maxItemsPorAlumno(req.getMaxItemsPorAlumno())
                .tiempoMaximoHoras(req.getTiempoMaximoHoras())
                .permiteExtension(Boolean.TRUE.equals(req.getPermiteExtension()))
                .horasExtension(req.getHorasExtension())
                .build();
        return toResponse(repository.save(c));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaUpdateRequest req) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        CategoriaPolitica c = buscarPorId(id);
        if (req.getNombreCategoria() != null) c.setNombreCategoria(req.getNombreCategoria());
        if (req.getMaxItemsPorAlumno() != null) c.setMaxItemsPorAlumno(req.getMaxItemsPorAlumno());
        if (req.getTiempoMaximoHoras() != null) c.setTiempoMaximoHoras(req.getTiempoMaximoHoras());
        if (req.getPermiteExtension() != null) c.setPermiteExtension(req.getPermiteExtension());
        if (req.getHorasExtension() != null) c.setHorasExtension(req.getHorasExtension());
        return toResponse(repository.save(c));
    }

    @Transactional
    public void desactivar(Long id) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        CategoriaPolitica c = buscarPorId(id);
        c.setActivo(false);
        repository.save(c);
    }

    public CategoriaPolitica buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("CategoriaPolitica", id));
    }

    private CategoriaResponse toResponse(CategoriaPolitica c) {
        return CategoriaResponse.builder()
                .id(c.getId())
                .nombreCategoria(c.getNombreCategoria())
                .maxItemsPorAlumno(c.getMaxItemsPorAlumno())
                .tiempoMaximoHoras(c.getTiempoMaximoHoras())
                .permiteExtension(c.getPermiteExtension())
                .horasExtension(c.getHorasExtension())
                .activo(c.getActivo())
                .build();
    }
}
