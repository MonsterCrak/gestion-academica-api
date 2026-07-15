package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.ChangePasswordRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ForgotPasswordRequest;
import edu.upc.sistema.gestionacademicaapi.dto.LoginRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ResetPasswordRequest;
import edu.upc.sistema.gestionacademicaapi.dto.TokenResponse;
import edu.upc.sistema.gestionacademicaapi.dto.UsuarioResponse;
import edu.upc.sistema.gestionacademicaapi.entity.PasswordResetToken;
import edu.upc.sistema.gestionacademicaapi.entity.TokenRevocado;
import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.exception.ReglaNegocioException;
import edu.upc.sistema.gestionacademicaapi.repository.PasswordResetTokenRepository;
import edu.upc.sistema.gestionacademicaapi.repository.TokenRevocadoRepository;
import edu.upc.sistema.gestionacademicaapi.repository.UsuarioRepository;
import edu.upc.sistema.gestionacademicaapi.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    /** HU-01: intentos fallidos permitidos antes de bloquear la cuenta. */
    private static final int MAX_INTENTOS_FALLIDOS = 5;

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final TokenRevocadoRepository tokenRevocadoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;
    private final AuditoriaService auditoriaService;
    private final NotificacionService notificacionService;

    @Value("${app.frontend.reset-url:http://localhost:4200/reset-password}")
    private String resetUrlBase;

    /** HU-01: login con emision de JWT, auditoria y bloqueo tras N intentos fallidos. */
    public TokenResponse login(LoginRequest req) {
        Optional<Usuario> opt = usuarioRepository.findByIdentificadorCorporativo(req.getIdentificadorCorporativo());
        if (opt.isEmpty()) {
            auditoriaService.registrarComo(req.getIdentificadorCorporativo(), AuditoriaService.LOGIN_FALLIDO,
                    "Usuario", null, AuditoriaService.DENEGADO, "Identificador inexistente");
            throw new BadCredentialsException("Credenciales invalidas");
        }

        Usuario usuario = opt.get();

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            auditoriaService.registrarComo(usuario.getIdentificadorCorporativo(), AuditoriaService.LOGIN_FALLIDO,
                    "Usuario", String.valueOf(usuario.getId()), AuditoriaService.DENEGADO, "Cuenta bloqueada o inactiva");
            throw new BadCredentialsException("La cuenta esta bloqueada. Use 'olvide mi contrasena' o contacte al administrador.");
        }

        if (!passwordEncoder.matches(req.getPassword(), usuario.getPasswordHash())) {
            int intentos = (usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos()) + 1;
            usuario.setIntentosFallidos(intentos);
            String detalle = "Password incorrecta (intento " + intentos + "/" + MAX_INTENTOS_FALLIDOS + ")";
            if (intentos >= MAX_INTENTOS_FALLIDOS) {
                usuario.setActivo(false);
                detalle = "Cuenta bloqueada tras " + intentos + " intentos fallidos";
            }
            usuarioRepository.save(usuario);
            auditoriaService.registrarComo(usuario.getIdentificadorCorporativo(), AuditoriaService.LOGIN_FALLIDO,
                    "Usuario", String.valueOf(usuario.getId()), AuditoriaService.DENEGADO, detalle);
            throw new BadCredentialsException("Credenciales invalidas");
        }

        if (usuario.getIntentosFallidos() != null && usuario.getIntentosFallidos() != 0) {
            usuario.setIntentosFallidos(0);
            usuarioRepository.save(usuario);
        }

        String token = jwtService.generarToken(
                usuario.getIdentificadorCorporativo(),
                usuario.getTipoUsuario().name(),
                usuario.getNombre(),
                usuario.getApellidos());

        auditoriaService.registrarComo(usuario.getIdentificadorCorporativo(), AuditoriaService.LOGIN,
                "Usuario", String.valueOf(usuario.getId()), AuditoriaService.OK, "Inicio de sesion exitoso");

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

    /**
     * Cierra la sesion revocando el jti del access token actual. El token es stateless
     * y sigue siendo criptograficamente valido hasta su expiracion natural, pero
     * JwtAuthenticationFilter lo rechaza en cuanto queda registrado como revocado.
     */
    @Transactional
    public void logout(String authHeader) {
        Usuario yo = currentUserService.obtenerActual();

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer ".length()).trim();
            try {
                Claims claims = jwtService.parseClaims(token);
                if (claims.getId() != null && claims.getExpiration() != null) {
                    LocalDateTime expiracion = claims.getExpiration().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    tokenRevocadoRepository.save(TokenRevocado.builder()
                            .jti(claims.getId())
                            .fechaExpiracion(expiracion)
                            .build());
                }
            } catch (JwtException ex) {
                log.debug("Token invalido en logout, nada que revocar: {}", ex.getMessage());
            }
        }

        auditoriaService.registrar(AuditoriaService.LOGOUT, "Usuario",
                String.valueOf(yo.getId()), AuditoriaService.OK, "Cierre de sesion");
    }

    /** HU-04: cambio de contrasena del propio usuario autenticado. */
    @Transactional
    public void cambiarPassword(ChangePasswordRequest req) {
        Usuario yo = currentUserService.obtenerActual();
        if (!passwordEncoder.matches(req.getPasswordActual(), yo.getPasswordHash())) {
            auditoriaService.registrar(AuditoriaService.CAMBIO_PASSWORD, "Usuario",
                    String.valueOf(yo.getId()), AuditoriaService.DENEGADO, "Password actual incorrecta");
            throw new ReglaNegocioException("PASSWORD_INCORRECTA", "La contrasena actual es incorrecta");
        }
        yo.setPasswordHash(passwordEncoder.encode(req.getPasswordNueva()));
        usuarioRepository.save(yo);
        auditoriaService.registrar(AuditoriaService.CAMBIO_PASSWORD, "Usuario",
                String.valueOf(yo.getId()), AuditoriaService.OK, "Contrasena actualizada por el usuario");
    }

    /** HU-03: solicita recuperacion; genera token temporal y envia enlace por correo. */
    @Transactional
    public void solicitarRecuperacion(ForgotPasswordRequest req) {
        Optional<Usuario> opt = usuarioRepository.findByEmailIgnoreCase(req.getEmail());
        // Se responde 200 siempre para no revelar si el correo existe.
        if (opt.isEmpty()) {
            log.info("Solicitud de recuperacion para correo no registrado: {}", req.getEmail());
            return;
        }
        Usuario u = opt.get();
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenRepository.save(PasswordResetToken.builder()
                .usuario(u)
                .token(token)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(30))
                .usado(false)
                .build());

        String enlace = resetUrlBase + "?token=" + token;
        notificacionService.notificar(u, NotificacionService.RECUPERAR_PASSWORD,
                "Recuperacion de contrasena",
                "Hola " + u.getNombre() + ", para restablecer tu contrasena ingresa a este enlace "
                        + "(valido por 30 minutos):\n" + enlace);

        auditoriaService.registrarComo(u.getIdentificadorCorporativo(), AuditoriaService.RESET_PASSWORD,
                "Usuario", String.valueOf(u.getId()), AuditoriaService.OK, "Solicitud de recuperacion enviada");
    }

    /** HU-03: restablece la contrasena a partir de un token valido y lo invalida. */
    @Transactional
    public void restablecerPassword(ResetPasswordRequest req) {
        PasswordResetToken prt = tokenRepository.findByToken(req.getToken())
                .orElseThrow(() -> new ReglaNegocioException("TOKEN_INVALIDO", "Enlace de recuperacion invalido"));
        if (Boolean.TRUE.equals(prt.getUsado())) {
            throw new ReglaNegocioException("TOKEN_USADO", "El enlace de recuperacion ya fue utilizado");
        }
        if (prt.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new ReglaNegocioException("TOKEN_EXPIRADO", "El enlace de recuperacion expiro");
        }

        Usuario u = prt.getUsuario();
        u.setPasswordHash(passwordEncoder.encode(req.getNuevaPassword()));
        u.setIntentosFallidos(0);
        if (Boolean.FALSE.equals(u.getActivo())) {
            u.setActivo(true); // el reset rehabilita la cuenta bloqueada por intentos fallidos
        }
        usuarioRepository.save(u);

        prt.setUsado(true);
        tokenRepository.save(prt);

        auditoriaService.registrar(AuditoriaService.RESET_PASSWORD, "Usuario",
                String.valueOf(u.getId()), AuditoriaService.OK, "Contrasena restablecida mediante token");
    }
}
