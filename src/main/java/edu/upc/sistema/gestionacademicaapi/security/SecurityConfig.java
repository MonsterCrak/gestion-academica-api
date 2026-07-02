package edu.upc.sistema.gestionacademicaapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.upc.sistema.gestionacademicaapi.exception.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/dev/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/espacios/**", "/recursos/**", "/categorias/**", "/materias/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/espacios", "/recursos", "/categorias", "/materias").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            GlobalExceptionHandler.ErrorResponse body = GlobalExceptionHandler.ErrorResponse.builder()
                                    .status(401)
                                    .codigo("NO_AUTENTICADO")
                                    .mensaje("Se requiere un token JWT valido")
                                    .timestamp(LocalDateTime.now())
                                    .traceId(UUID.randomUUID())
                                    .build();
                            mapper.writeValue(res.getOutputStream(), body);
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            GlobalExceptionHandler.ErrorResponse body = GlobalExceptionHandler.ErrorResponse.builder()
                                    .status(403)
                                    .codigo("ACCESO_DENEGADO")
                                    .mensaje("No tiene permisos para esta operacion")
                                    .timestamp(LocalDateTime.now())
                                    .traceId(UUID.randomUUID())
                                    .build();
                            mapper.writeValue(res.getOutputStream(), body);
                        }))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
