package edu.upc.sistema.gestionacademicaapi.security;

import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.repository.TokenRevocadoRepository;
import edu.upc.sistema.gestionacademicaapi.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final TokenRevocadoRepository tokenRevocadoRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIX.length()).trim();
        try {
            Claims claims = jwtService.parseClaims(token);
            String identificador = claims.getSubject();

            if (identificador == null) {
                log.debug("Token sin subject");
                chain.doFilter(request, response);
                return;
            }

            if (claims.getId() != null && tokenRevocadoRepository.existsByJti(claims.getId())) {
                log.debug("Token revocado (logout): {}", identificador);
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }

            Optional<Usuario> opt = usuarioRepository.findByIdentificadorCorporativo(identificador);
            if (opt.isEmpty()) {
                log.debug("Usuario del token no existe en BD: {}", identificador);
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }

            Usuario usuario = opt.get();
            if (Boolean.FALSE.equals(usuario.getActivo())) {
                log.debug("Usuario inactivo: {}", identificador);
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }

            String rol = "ROLE_" + usuario.getTipoUsuario().name();
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    identificador,
                    null,
                    List.of(new SimpleGrantedAuthority(rol))
            );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException ex) {
            log.debug("Token JWT invalido: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}