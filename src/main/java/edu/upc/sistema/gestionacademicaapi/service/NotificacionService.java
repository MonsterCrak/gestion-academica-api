package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.NotificacionResponse;
import edu.upc.sistema.gestionacademicaapi.entity.Notificacion;
import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoNotificacion;
import edu.upc.sistema.gestionacademicaapi.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Persiste y envia notificaciones a los usuarios (HU-23). Registra el estado de
 * envio (PENDIENTE/ENVIADO/FALLIDO) y el numero de intentos.
 */
@Service
@RequiredArgsConstructor
public class NotificacionService {

    // Tipos de notificacion (HU-14, HU-17, HU-20, HU-23)
    public static final String AVAL_RESERVA = "AVAL_RESERVA";
    public static final String RESERVA_RESUELTA = "RESERVA_RESUELTA";
    public static final String TUTORIA_CONFIRMADA = "TUTORIA_CONFIRMADA";
    public static final String TUTORIA_LISTA_ESPERA = "TUTORIA_LISTA_ESPERA";
    public static final String PENALIZACION = "PENALIZACION";
    public static final String VENCIMIENTO_PRESTAMO = "VENCIMIENTO_PRESTAMO";
    public static final String RECUPERAR_PASSWORD = "RECUPERAR_PASSWORD";

    private final NotificacionRepository repository;
    private final CorreoService correoService;
    private final CurrentUserService currentUserService;

    /** Crea, envia y registra el estado de una notificacion. No propaga errores de envio. */
    @Transactional
    public void notificar(Usuario usuario, String tipo, String asunto, String cuerpo) {
        Notificacion n = repository.save(Notificacion.builder()
                .usuario(usuario)
                .tipo(tipo)
                .asunto(asunto)
                .cuerpo(cuerpo)
                .estadoEnvio(EstadoNotificacion.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .intentos(0)
                .build());

        boolean ok = correoService.enviar(usuario.getEmail(), asunto, cuerpo);
        n.setIntentos(1);
        n.setEstadoEnvio(ok ? EstadoNotificacion.ENVIADO : EstadoNotificacion.FALLIDO);
        if (ok) {
            n.setFechaEnvio(LocalDateTime.now());
        }
        repository.save(n);
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> misNotificaciones() {
        Usuario yo = currentUserService.obtenerActual();
        return repository.findByUsuario_IdOrderByFechaCreacionDesc(yo.getId())
                .stream().map(this::toResponse).toList();
    }

    private NotificacionResponse toResponse(Notificacion n) {
        return NotificacionResponse.builder()
                .id(n.getId())
                .tipo(n.getTipo())
                .asunto(n.getAsunto())
                .cuerpo(n.getCuerpo())
                .estadoEnvio(n.getEstadoEnvio())
                .fechaCreacion(n.getFechaCreacion())
                .fechaEnvio(n.getFechaEnvio())
                .intentos(n.getIntentos())
                .build();
    }
}
