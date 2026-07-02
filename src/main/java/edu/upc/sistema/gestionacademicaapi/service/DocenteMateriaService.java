package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.DocenteMateriaCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.DocenteMateriaResponse;
import edu.upc.sistema.gestionacademicaapi.entity.DocenteMateria;
import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.ReglaNegocioException;
import edu.upc.sistema.gestionacademicaapi.repository.DocenteMateriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocenteMateriaService {

    private final DocenteMateriaRepository repository;
    private final CurrentUserService currentUser;
    private final MateriaService materiaService;

    @Transactional(readOnly = true)
    public List<DocenteMateriaResponse> listarPorDocente(UUID docenteId) {
        return repository.findByDocente_IdAndActivoTrue(docenteId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DocenteMateriaResponse> listarMias() {
        Usuario yo = currentUser.obtenerActual();
        return listarPorDocente(yo.getId());
    }

    @Transactional
    public DocenteMateriaResponse registrar(DocenteMateriaCreateRequest req) {
        Usuario yo = currentUser.obtenerActual();
        if (yo.getTipoUsuario() != TipoUsuario.DOCENTE) {
            throw new ReglaNegocioException("TIPO_INVALIDO", "Solo los docentes pueden registrar preferencias");
        }
        materiaService.buscarPorId(req.getMateriaId());

        repository.findByDocente_IdAndMateria_IdAndActivoTrue(yo.getId(), req.getMateriaId())
                .ifPresent(existente -> {
                    throw new ReglaNegocioException("REPETIDO",
                            "Ya tiene una preferencia activa para esta materia");
                });

        DocenteMateria dm = DocenteMateria.builder()
                .docente(yo)
                .materia(materiaService.buscarPorId(req.getMateriaId()))
                .tipoAulaRequerida(req.getTipoAulaRequerida())
                .modalidadAsignacion(req.getModalidadAsignacion())
                .fechaAlta(LocalDateTime.now())
                .activo(true)
                .build();
        return toResponse(repository.save(dm));
    }

    @Transactional
    public void desactivar(UUID id) {
        Usuario yo = currentUser.obtenerActual();
        DocenteMateria dm = repository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("NO_ENCONTRADO", "Preferencia no encontrada"));
        if (!dm.getDocente().getId().equals(yo.getId())
                && yo.getTipoUsuario() != TipoUsuario.ADMINISTRATIVO) {
            throw new ReglaNegocioException("ACCESO_DENEGADO", "No es dueno de esta preferencia");
        }
        dm.setActivo(false);
        repository.save(dm);
    }

    private DocenteMateriaResponse toResponse(DocenteMateria d) {
        return DocenteMateriaResponse.builder()
                .id(d.getId())
                .docenteId(d.getDocente().getId())
                .materiaId(d.getMateria().getId())
                .tipoAulaRequerida(d.getTipoAulaRequerida())
                .modalidadAsignacion(d.getModalidadAsignacion())
                .fechaAlta(d.getFechaAlta())
                .activo(d.getActivo())
                .build();
    }
}
