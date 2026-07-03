package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.AccesoNoAutorizadoException;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UsuarioRepository usuarioRepository;

    public Usuario obtenerActual() {
        String identificador = identificadorAutenticado();
        return usuarioRepository.findByIdentificadorCorporativo(identificador)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", identificador));
    }

    public String identificadorAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccesoNoAutorizadoException("No hay sesion activa");
        }
        return auth.getName();
    }

    public void exigirTipo(TipoUsuario... tiposPermitidos) {
        Usuario u = obtenerActual();
        for (TipoUsuario t : tiposPermitidos) {
            if (u.getTipoUsuario() == t) return;
        }
        throw new AccesoNoAutorizadoException(
                "Esta operacion requiere tipo de usuario: " + java.util.Arrays.toString(tiposPermitidos));
    }
}
