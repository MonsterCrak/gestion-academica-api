package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.UsuarioCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.UsuarioResponse;
import edu.upc.sistema.gestionacademicaapi.dto.UsuarioUpdateRequest;
import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.AccesoNoAutorizadoException;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.exception.ReglaNegocioException;
import edu.upc.sistema.gestionacademicaapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest req) {
        currentUserService.exigirTipo(TipoUsuario.ADMINISTRATIVO);

        if (usuarioRepository.existsByIdentificadorCorporativo(req.getIdentificadorCorporativo())) {
            throw new ReglaNegocioException("IDENTIFICADOR_DUPLICADO",
                    "Ya existe un usuario con ese identificador corporativo");
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()
                && usuarioRepository.existsByEmailIgnoreCase(req.getEmail())) {
            throw new ReglaNegocioException("EMAIL_DUPLICADO",
                    "Ya existe un usuario con ese correo institucional");
        }

        Usuario u = Usuario.builder()
                .tipoUsuario(req.getTipoUsuario())
                .identificadorCorporativo(req.getIdentificadorCorporativo())
                .email(req.getEmail())
                .nombre(req.getNombre())
                .apellidos(req.getApellidos())
                .carreraId(req.getCarreraId())
                .activo(true)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .build();

        Usuario saved = usuarioRepository.save(u);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable) {
        currentUserService.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        return usuarioRepository.findAll(pageable).map(this::toResponse);
    }

    /** Lista usuarios activos de un tipo (p. ej. docentes avalistas). Accesible a cualquier autenticado. */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarPorTipo(TipoUsuario tipo) {
        currentUserService.obtenerActual();
        return usuarioRepository.findByTipoUsuarioAndActivoTrueOrderByApellidosAsc(tipo)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        Usuario yo = currentUserService.obtenerActual();
        if (yo.getTipoUsuario() != TipoUsuario.ADMINISTRATIVO && !yo.getId().equals(id)) {
            throw new AccesoNoAutorizadoException("Solo puede ver su propio perfil");
        }
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
        return toResponse(u);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorIdentificador(String identificador) {
        Usuario yo = currentUserService.obtenerActual();
        if (yo.getTipoUsuario() != TipoUsuario.ADMINISTRATIVO
                && !yo.getIdentificadorCorporativo().equals(identificador)) {
            throw new AccesoNoAutorizadoException("Solo puede ver su propio perfil");
        }
        Usuario u = usuarioRepository.findByIdentificadorCorporativo(identificador)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", identificador));
        return toResponse(u);
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest req) {
        Usuario yo = currentUserService.obtenerActual();
        boolean esAdmin = yo.getTipoUsuario() == TipoUsuario.ADMINISTRATIVO;

        if (!esAdmin && !yo.getId().equals(id)) {
            throw new AccesoNoAutorizadoException("Solo puede editar su propio perfil");
        }
        if (!esAdmin && req.getTipoUsuario() != null
                && req.getTipoUsuario() != yo.getTipoUsuario()) {
            throw new AccesoNoAutorizadoException("No puede cambiar su propio tipo de usuario");
        }

        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));

        if (req.getIdentificadorCorporativo() != null
                && !req.getIdentificadorCorporativo().equals(u.getIdentificadorCorporativo())) {
            if (usuarioRepository.existsByIdentificadorCorporativo(req.getIdentificadorCorporativo())) {
                throw new ReglaNegocioException("IDENTIFICADOR_DUPLICADO",
                        "Ya existe un usuario con ese identificador corporativo");
            }
            u.setIdentificadorCorporativo(req.getIdentificadorCorporativo());
        }
        if (req.getEmail() != null && !req.getEmail().equalsIgnoreCase(u.getEmail())) {
            if (usuarioRepository.existsByEmailIgnoreCase(req.getEmail())) {
                throw new ReglaNegocioException("EMAIL_DUPLICADO",
                        "Ya existe un usuario con ese correo institucional");
            }
            u.setEmail(req.getEmail());
        }
        if (req.getTipoUsuario() != null) {
            u.setTipoUsuario(req.getTipoUsuario());
        }
        if (req.getNombre() != null) {
            u.setNombre(req.getNombre());
        }
        if (req.getApellidos() != null) {
            u.setApellidos(req.getApellidos());
        }
        if (req.getCarreraId() != null) {
            u.setCarreraId(req.getCarreraId());
        }
        if (req.getPassword() != null) {
            u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }

        Usuario saved = usuarioRepository.save(u);
        return toResponse(saved);
    }

    @Transactional
    public void desactivar(Long id) {
        currentUserService.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
        u.setActivo(false);
        usuarioRepository.save(u);
    }

    private UsuarioResponse toResponse(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .tipoUsuario(u.getTipoUsuario())
                .identificadorCorporativo(u.getIdentificadorCorporativo())
                .email(u.getEmail())
                .nombre(u.getNombre())
                .apellidos(u.getApellidos())
                .carreraId(u.getCarreraId())
                .activo(u.getActivo())
                .estado(u.getEstado())
                .tieneDeuda(u.getTieneDeuda())
                .penalizadoHasta(u.getPenalizadoHasta())
                .bloqueadoParaOperar(u.isBloqueadoParaOperar())
                .build();
    }

    public void reactivar(Long id) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        currentUserService.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
        try {
            usuarioRepository.delete(u);
            usuarioRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ReglaNegocioException("USUARIO_CON_REGISTROS",
                    "No se puede eliminar: el usuario tiene registros asociados. Use desactivar en su lugar.");
        }
    }
}
