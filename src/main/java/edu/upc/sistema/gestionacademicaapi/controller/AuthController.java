package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.ChangePasswordRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ForgotPasswordRequest;
import edu.upc.sistema.gestionacademicaapi.dto.LoginRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ResetPasswordRequest;
import edu.upc.sistema.gestionacademicaapi.dto.TokenResponse;
import edu.upc.sistema.gestionacademicaapi.dto.UsuarioResponse;
import edu.upc.sistema.gestionacademicaapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UsuarioResponse me() {
        return authService.me();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> cambiarPassword(@Valid @RequestBody ChangePasswordRequest req) {
        authService.cambiarPassword(req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> olvidePassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.solicitarRecuperacion(req);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.restablecerPassword(req);
        return ResponseEntity.noContent().build();
    }
}
