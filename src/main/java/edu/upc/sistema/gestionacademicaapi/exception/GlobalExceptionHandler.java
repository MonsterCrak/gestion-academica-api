package edu.upc.sistema.gestionacademicaapi.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Getter
    @Builder
    public static class ErrorResponse {
        private int status;
        private String codigo;
        private String mensaje;
        private LocalDateTime timestamp;
        private UUID traceId;
        private List<String> detalles;
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> handleReglaNegocio(ReglaNegocioException ex) {
        log.warn("Regla de negocio violada [{}]: {}", ex.getCodigo(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .codigo(ex.getCodigo())
                        .mensaje(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .traceId(UUID.randomUUID())
                        .build());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .codigo("RECURSO_NO_ENCONTRADO")
                        .mensaje(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .traceId(UUID.randomUUID())
                        .build());
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<ErrorResponse> handleAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
        log.warn("Acceso no autorizado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .codigo("ACCESO_NO_AUTORIZADO")
                        .mensaje(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .traceId(UUID.randomUUID())
                        .build());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        log.warn("Fallo de autenticacion: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .codigo("NO_AUTENTICADO")
                        .mensaje("Credenciales invalidas o sesion expirada")
                        .timestamp(LocalDateTime.now())
                        .traceId(UUID.randomUUID())
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .codigo("ACCESO_DENEGADO")
                        .mensaje("No tiene permisos para realizar esta operacion")
                        .timestamp(LocalDateTime.now())
                        .traceId(UUID.randomUUID())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());
        log.warn("Error de validacion: {}", detalles);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .codigo("VALIDACION")
                        .mensaje("Uno o mas campos son invalidos")
                        .timestamp(LocalDateTime.now())
                        .traceId(UUID.randomUUID())
                        .detalles(detalles)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        UUID traceId = UUID.randomUUID();
        log.error("Error interno no controlado [{}]: {}", traceId, ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .codigo("ERROR_INTERNO")
                        .mensaje("Error inesperado en el servidor")
                        .timestamp(LocalDateTime.now())
                        .traceId(traceId)
                        .build());
    }
}
