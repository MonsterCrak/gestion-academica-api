package edu.upc.sistema.gestionacademicaapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
@ConfigurationProperties(prefix = "app.security.jwt")
@Getter
@Setter
public class JwtService {

    private String secret;
    private String issuer;
    private Integer expirationMinutes;

    private SecretKey claveFirma() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(String identificadorCorporativo, String tipoUsuario, String nombre, String apellidos) {
        Date ahora = new Date();
        Date expiracion = Date.from(LocalDateTime.now()
                .plusMinutes(expirationMinutes == null ? 60 : expirationMinutes)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .subject(identificadorCorporativo)
                .claim("tipoUsuario", tipoUsuario)
                .claim("nombre", nombre)
                .claim("apellidos", apellidos)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(claveFirma())
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(claveFirma())
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long expirationSegundos() {
        return (expirationMinutes == null ? 60 : expirationMinutes) * 60L;
    }

    public Map<String, Object> infoExtra(String token) {
        Claims c = parseClaims(token);
        return Map.of(
                "identificador", c.getSubject(),
                "tipoUsuario", c.get("tipoUsuario"),
                "nombre", c.get("nombre"),
                "apellidos", c.get("apellidos")
        );
    }
}
