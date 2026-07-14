package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.LoginRequest;
import edu.upc.sistema.gestionacademicaapi.dto.TokenResponse;
import edu.upc.sistema.gestionacademicaapi.dto.UsuarioResponse;
import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.repository.UsuarioRepository;
import edu.upc.sistema.gestionacademicaapi.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    public TokenResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByIdentificadorCorporativo(req.getIdentificadorCorporativo())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }

        if (!passwordEncoder.matches(req.getPassword(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }

        String token = jwtService.generarToken(
                usuario.getIdentificadorCorporativo(),
                usuario.getTipoUsuario().name(),
                usuario.getNombre(),
                usuario.getApellidos());

        return TokenResponse.builder()
                .token(token)
                .tipoUsuario(usuario.getTipoUsuario().name())
                .identificadorCorporativo(usuario.getIdentificadorCorporativo())
                .nombre(usuario.getNombre())
                .expiresInSegundos(jwtService.expirationSegundos())
                .build();
    }

    public UsuarioResponse me() {
        Usuario u = currentUserService.obtenerActual();
        return UsuarioResponse.builder()
                .id(u.getId())
                .tipoUsuario(u.getTipoUsuario())
                .identificadorCorporativo(u.getIdentificadorCorporativo())
                .nombre(u.getNombre())
                .apellidos(u.getApellidos())
                .carreraId(u.getCarreraId())
                .activo(u.getActivo())
                .build();
    }
}