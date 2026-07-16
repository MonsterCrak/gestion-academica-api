package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.DeudaUpdateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.PenalizacionCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.PenalizacionResponse;
import edu.upc.sistema.gestionacademicaapi.entity.Penalizacion;
import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoUsuario;
import edu.upc.sistema.gestionacademicaapi.enums.ModoResolucion;
import edu.upc.sistema.gestionacademicaapi.enums.OrigenPenalizacion;
import edu.upc.sistema.gestionacademicaapi.enums.TipoPenalizacion;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.AccesoNoAutorizadoException;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.exception.ReglaNegocioException;
import edu.upc.sistema.gestionacademicaapi.repository.PenalizacionRepository;
import edu.upc.sistema.gestionacademicaapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        return crear(usuario, TipoPenalizacion.RETRASO_DEVOLUCION, OrigenPenalizacion.PRESTAMO, motivo, dias, prestamoId,
                ModoResolucion.SUSPENSION_RECURSOS, null,
                "La penalizacion se levanta automaticamente al vencer el bloqueo. Devuelve el equipo a tiempo en adelante.");
    }

    /** HU-20: penalizacion manual creada por un administrador. */
    @Transactional
    public PenalizacionResponse aplicarManual(PenalizacionCreateRequest req) {
        currentUserService.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        Usuario usuario = usuarioRepository.findById(req.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", req.getUsuarioId()));
        OrigenPenalizacion origen = req.getOrigen() != null ? req.getOrigen() : OrigenPenalizacion.GENERAL;
        ModoResolucion modo = req.getModoResolucion() != null ? req.getModoResolucion() : ModoResolucion.SUSPENSION_RECURSOS;
        Penalizacion p = crear(usuario, req.getTipo(), origen, req.getMotivo(), req.getDiasBloqueo(), null,
                modo, req.getMonto(), req.getInstruccionesResolucion());
        return toResponse(p);
    }

    /** El administrador levanta manualmente una penalización vigente (la resuelve antes de su vencimiento). */
    @Transactional
    public PenalizacionResponse levantar(Long id) {
        currentUserService.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        Penalizacion p = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Penalizacion", id));
        if (!Boolean.TRUE.equals(p.getActiva())) {
            throw new ReglaNegocioException("ESTADO_INVALIDO", "La penalizacion ya no esta vigente");
        }
        p.setActiva(false);
        Penalizacion saved = repository.save(p);

        Usuario u = p.getUsuario();
        recomputarBloqueo(u);
        usuarioRepository.save(u);

        auditoriaService.registrar(AuditoriaService.LEVANTA_PENALIZACION, "Penalizacion",
                String.valueOf(p.getId()), AuditoriaService.OK, "Penalizacion levantada manualmente por administrador");
        notificacionService.notificar(u, NotificacionService.PENALIZACION, "Penalizacion levantada",
                "Tu penalizacion por \"" + p.getMotivo() + "\" fue levantada. "
                        + (u.isBloqueadoParaOperar()
                            ? "Aun tienes otras restricciones vigentes en tu cuenta."
                            : "Tu cuenta vuelve a estar habilitada para operar."));
        return toResponse(saved);
    }

    /** Reenvía al usuario un mensaje con el detalle de cómo resolver la penalización. */
    @Transactional
    public PenalizacionResponse enviarMensaje(Long id, String mensajeExtra) {
        currentUserService.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        Penalizacion p = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Penalizacion", id));
        String cuerpo = describirResolucion(p);
        if (mensajeExtra != null && !mensajeExtra.isBlank()) {
            cuerpo = mensajeExtra.trim() + "\n\n" + cuerpo;
        }
        notificacionService.notificar(p.getUsuario(), NotificacionService.PENALIZACION,
                "Como resolver tu penalizacion", cuerpo);
        auditoriaService.registrar(AuditoriaService.APLICA_PENALIZACION, "Penalizacion",
                String.valueOf(p.getId()), AuditoriaService.OK, "Mensaje de resolucion enviado al usuario");
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
    public List<PenalizacionResponse> misPenalizaciones() {
        Usuario yo = currentUserService.obtenerActual();
        return repository.findByUsuario_IdOrderByFechaInicioDesc(yo.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PenalizacionResponse> listarDeUsuario(Long usuarioId) {
        Usuario yo = currentUserService.obtenerActual();
        if (yo.getTipoUsuario() != TipoUsuario.ADMINISTRATIVO && !yo.getId().equals(usuarioId)) {
            throw new AccesoNoAutorizadoException("Solo puede ver sus propias penalizaciones");
        }
        return repository.findByUsuario_IdOrderByFechaInicioDesc(usuarioId)
                .stream().map(this::toResponse).toList();
    }

    // --- internos ---

    private Penalizacion crear(Usuario usuario, TipoPenalizacion tipo, OrigenPenalizacion origen,
                               String motivo, long diasBloqueo, Long prestamoId,
                               ModoResolucion modoResolucion, java.math.BigDecimal monto, String instrucciones) {
        LocalDateTime ahora = LocalDateTime.now();
        ModoResolucion modo = modoResolucion != null ? modoResolucion : ModoResolucion.SUSPENSION_RECURSOS;
        Penalizacion p = repository.save(Penalizacion.builder()
                .usuario(usuario)
                .tipo(tipo)
                .origen(origen != null ? origen : OrigenPenalizacion.GENERAL)
                .motivo(motivo)
                .modoResolucion(modo)
                .monto(modo == ModoResolucion.SUSPENSION_RECURSOS ? null : monto)
                .instruccionesResolucion(instrucciones)
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
                motivo + ".\n\n" + describirResolucion(p));
        return p;
    }

    /** Construye un texto legible con el detalle y la forma de resolver la penalización. */
    private String describirResolucion(Penalizacion p) {
        StringBuilder sb = new StringBuilder();
        sb.append("Penalizado hasta: ").append(p.getFechaFin()).append(".\n");
        ModoResolucion modo = p.getModoResolucion() != null ? p.getModoResolucion() : ModoResolucion.SUSPENSION_RECURSOS;
        switch (modo) {
            case PAGO_PENSION -> sb.append("Para resolverlo: se cargara un monto de S/ ")
                    .append(p.getMonto() != null ? p.getMonto() : "0")
                    .append(" en tu siguiente pension.");
            case DESCUENTO_HABERES -> sb.append("Para resolverlo: se aplicara un descuento de S/ ")
                    .append(p.getMonto() != null ? p.getMonto() : "0")
                    .append(" en tus haberes/planilla.");
            case SUSPENSION_RECURSOS -> sb.append(
                    "Para resolverlo: cumple el periodo de suspension del uso de recursos de la universidad.");
        }
        if (p.getInstruccionesResolucion() != null && !p.getInstruccionesResolucion().isBlank()) {
            sb.append("\n").append(p.getInstruccionesResolucion());
        }
        return sb.toString();
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
                .origen(p.getOrigen())
                .motivo(p.getMotivo())
                .modoResolucion(p.getModoResolucion())
                .monto(p.getMonto())
                .instruccionesResolucion(p.getInstruccionesResolucion())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .activa(p.getActiva())
                .prestamoId(p.getPrestamoId())
                .build();
    }
}
