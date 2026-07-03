package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.DevTokenRequest;
import edu.upc.sistema.gestionacademicaapi.dto.TokenResponse;
import edu.upc.sistema.gestionacademicaapi.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dev")
@Profile({"dev", "default"})
@RequiredArgsConstructor
public class DevTokenController {

    private final JwtService jwtService;

    @PostMapping("/token")
    public TokenResponse emitir(@Valid @RequestBody DevTokenRequest req) {
        String token = jwtService.generarToken(
                req.getIdentificadorCorporativo(),
                req.getTipoUsuario(),
                req.getNombre() == null ? "" : req.getNombre(),
                req.getApellidos() == null ? "" : req.getApellidos());
        return TokenResponse.builder()
                .token(token)
                .tipoUsuario(req.getTipoUsuario())
                .identificadorCorporativo(req.getIdentificadorCorporativo())
                .nombre(req.getNombre())
                .expiresInSegundos(jwtService.expirationSegundos())
                .build();
    }
}
