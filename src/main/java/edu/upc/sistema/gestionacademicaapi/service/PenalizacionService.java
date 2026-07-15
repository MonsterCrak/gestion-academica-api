package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.DeudaUpdateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.PenalizacionCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.PenalizacionResponse;
import edu.upc.sistema.gestionacademicaapi.entity.Penalizacion;
import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoUsuario;
import edu.upc.sistema.gestionacademicaapi.enums.TipoPenalizacion;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.AccesoNoAutorizadoException;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.exception.ReglaNegocioException;
import edu.upc.sistema.gestionacademicaapi.repository.PenalizacionRepository;
import edu.upc.sistema.gestionacademicaapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Motor de penalizaciones y bloqueo por deuda (HU-19, HU-20; RN-01, RN-09).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PenalizacionService {

    private final PenalizacionRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final CurrentUserService currentUserService;
    private final AuditoriaService auditoriaService;
    private final NotificacionService notificacionService;

    /**
     * RN-01/RN-09: valida que el usuario pueda operar (sin deuda ni penalizacion vigente).
     * Registra el bloqueo en la bitacora y lanza excepcion si corresponde.
     */
    public void verificarPuedeOperar(Usuario usuario, String operacion) {
        if (Boolean.TRUE.equals(usuario.getTieneDeuda())) {
            auditoriaService.registrar(AuditoriaService.BLOQUEO_POR_REGLA, "Usuario",
                    String.valueOf(usuario.getId()), AuditoriaService.DENEGADO,
                    operacion + " denegada: deuda financiera pendiente (RN-01)");
            throw new ReglaNegocioException("RN-01",
                    "Operacion no permitida: tiene una deuda financiera pendiente");
        }
        if (usuario.isBloqueadoParaOperar()) {
            auditoriaService.registrar(AuditoriaService.BLOQUEO_POR_REGLA, "Usuario",
                    String.valueOf(usuario.getId()), AuditoriaService.DENEGADO,
                    operacion + " denegada: penalizacion vigente hasta " + usuario.getPenalizadoHasta() + " (RN-09)");
            throw new ReglaNegocioException("RN-09",
                    "Operacion no permitida: tiene una penalizacion vigente hasta " + usuario.getPenalizadoHasta());
        }
    }

    /** HU-20: aplica una penalizacion por retraso en la devolucion. */
    @Transactional
    public Penalizacion aplicarPorRetraso(Usuario usuario, Long prestamoId, long diasRetraso) {
        long dias = Math.max(1, diasRetraso);
        String motivo = "Devolucion fuera de plazo (" + diasRetraso + " dia(s) de retraso)";
        return crear(usuario, TipoPenalizacion.RETRASO_DEVOLUCION, motivo, dias, prestamoId);
    }

    /** HU-20: penalizacion manual creada por un administrador. */
    @Transactional
    public PenalizacionResponse aplicarManual(PenalizacionCreateRequest req) {
        currentUserService.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        Usuario usuario = usuarioRepository.findById(req.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", req.getUsuarioId()));
        Penalizacion p = crear(usuario, req.getTipo(), req.getMotivo(), req.getDiasBloqueo(), null);
        return toResponse(p);
    }

    /** HU-19: activa o desactiva la deuda financiera de un usuario (bloqueo por deuda). */
    @Transactional
    public void actualizarDeuda(Long usuarioId, DeudaUpdateRequest req) {
        currentUserService.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", usuarioId));

        usuario.setTieneDeuda(req.getTieneDeuda());
        recomputarBloqueo(usuario);
        usuarioRepository.save(usuario);

        auditoriaService.registrar(AuditoriaService.APLICA_PENALIZACION, "Usuario",
                String.valueOf(usuarioId), AuditoriaService.OK,
                "Deuda financiera " + (Boolean.TRUE.equals(req.getTieneDeuda()) ? "ACTIVADA" : "LEVANTADA")
                        + (req.getMotivo() != null ? " — " + req.getMotivo() : ""));

        String asunto = Boolean.TRUE.equals(req.getTieneDeuda())
                ? "Cuenta bloqueada por deuda financiera"
                : "Deuda regularizada — cuenta habilitada";
        notificacionService.notificar(usuario, NotificacionService.PENALIZACION, asunto,
                "Estado de deuda actualizado. " + (req.getMotivo() != null ? req.getMotivo() : ""));
    }

    /** HU-20: job programado que levanta las penalizaciones vencidas. */
    @Transactional
    public int levantarVencidas() {
        List<Penalizacion> vencidas = repository.findByActivaTrueAndFechaFinBefore(LocalDateTime.now());
        int levantadas = 0;
        for (Penalizacion p : vencidas) {
            p.setActiva(false);
            repository.save(p);
            Usuario u = p.getUsuario();
            recomputarBloqueo(u);
            usuarioRepository.save(u);
            auditoriaService.registrarComo(null, AuditoriaService.LEVANTA_PENALIZACION, "Penalizacion",
                    String.valueOf(p.getId()), AuditoriaService.OK,
                    "Penalizacion vencida levantada para " + u.getIdentificadorCorporativo());
            levantadas++;
        }
        return levantadas;
    }

    @Transactional(readOnly = true)
    public Page<PenalizacionResponse> misPenalizaciones(Pageable pageable) {
        Usuario yo = currentUserService.obtenerActual();
        return repository.findByUsuario_Id(yo.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PenalizacionResponse> listarDeUsuario(Long usuarioId, Pageable pageable) {
        Usuario yo = currentUserService.obtenerActual();
        if (yo.getTipoUsuario() != TipoUsuario.ADMINISTRATIVO && !yo.getId().equals(usuarioId)) {
            throw new AccesoNoAutorizadoException("Solo puede ver sus propias penalizaciones");
        }
        return repository.findByUsuario_Id(usuarioId, pageable).map(this::toResponse);
    }

    // --- internos ---

    private Penalizacion crear(Usuario usuario, TipoPenalizacion tipo, String motivo, long diasBloqueo, Long prestamoId) {
        LocalDateTime ahora = LocalDateTime.now();
        Penalizacion p = repository.save(Penalizacion.builder()
                .usuario(usuario)
                .tipo(tipo)
                .motivo(motivo)
                .fechaInicio(ahora)
                .fechaFin(ahora.plusDays(diasBloqueo))
                .activa(true)
                .prestamoId(prestamoId)
                .build());

        recomputarBloqueo(usuario);
        usuarioRepository.save(usuario);

        auditoriaService.registrar(AuditoriaService.APLICA_PENALIZACION, "Penalizacion",
                String.valueOf(p.getId()), AuditoriaService.OK,
                tipo + " a " + usuario.getIdentificadorCorporativo() + " — " + motivo);

        notificacionService.notificar(usuario, NotificacionService.PENALIZACION,
                "Se aplico una penalizacion en tu cuenta",
                motivo + ". Bloqueo vigente hasta " + p.getFechaFin() + ".");
        return p;
    }

    /** Recalcula penalizadoHasta y estado a partir de deuda y penalizaciones activas. */
    private void recomputarBloqueo(Usuario u) {
        List<Penalizacion> activas = repository.findByUsuario_IdAndActivaTrue(u.getId());
        LocalDateTime hasta = activas.stream()
                .map(Penalizacion::getFechaFin)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
        u.setPenalizadoHasta(hasta);
        boolean bloqueado = Boolean.TRUE.equals(u.getTieneDeuda()) || !activas.isEmpty();
        u.setEstado(bloqueado ? EstadoUsuario.BLOQUEADO : EstadoUsuario.ACTIVO);
    }

    private PenalizacionResponse toResponse(Penalizacion p) {
        return PenalizacionResponse.builder()
                .id(p.getId())
                .usuarioId(p.getUsuario().getId())
                .usuarioIdentificador(p.getUsuario().getIdentificadorCorporativo())
                .tipo(p.getTipo())
                .motivo(p.getMotivo())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .activa(p.getActiva())
                .prestamoId(p.getPrestamoId())
                .build();
    }
}
