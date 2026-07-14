package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.AvalRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ReservaCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ReservaResponse;
import edu.upc.sistema.gestionacademicaapi.entity.EspacioFisico;
import edu.upc.sistema.gestionacademicaapi.entity.Reserva;
import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoReserva;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.AccesoNoAutorizadoException;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.exception.ReglaNegocioException;
import edu.upc.sistema.gestionacademicaapi.repository.EspacioFisicoRepository;
import edu.upc.sistema.gestionacademicaapi.repository.ReservaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reserva de aulas con aval docente (HU-13/14/15).
 * RN-05 aval obligatorio, RN-06 bloqueo de fines no académicos, anti-solapamiento de horarios.
 */
@Service
@RequiredArgsConstructor
public class ReservaService {

    /** RN-06: términos que evidencian un uso no académico y bloquean la reserva (HU-15). */
    private static final List<String> MOTIVOS_PROHIBIDOS = List.of(
            "lucro", "venta", "vender", "comercial", "fiesta", "celebracion", "cumpleaños", "cumpleanos",
            "proselitismo", "politic", "partido", "campaña", "campana electoral",
            "culto", "religios", "misa", "oracion", "alcohol");

    private final ReservaRepository repository;
    private final EspacioFisicoRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CurrentUserService currentUser;
    private final PenalizacionService penalizacionService;
    private final AuditoriaService auditoriaService;
    private final NotificacionService notificacionService;

    @Transactional
    public ReservaResponse solicitar(ReservaCreateRequest req) {
        Usuario yo = currentUser.obtenerActual();
        penalizacionService.verificarPuedeOperar(yo, "Reserva de aula"); // RN-01
        validarFinAcademico(req.getMotivo()); // HU-15 / RN-06

        if (!req.getFechaInicio().isBefore(req.getFechaFin())) {
            throw new ReglaNegocioException("FECHA_INVALIDA", "La hora de inicio debe ser anterior a la de fin");
        }
        if (req.getFechaInicio().isBefore(LocalDateTime.now())) {
            throw new ReglaNegocioException("FECHA_PASADA", "No se puede reservar en el pasado");
        }

        EspacioFisico espacio = espacioRepository.findById(req.getEspacioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("EspacioFisico", req.getEspacioId()));
        if (!Boolean.TRUE.equals(espacio.getActivo())) {
            throw new ReglaNegocioException("AULA_INACTIVA", "El aula no esta disponible");
        }
        if (!Boolean.TRUE.equals(espacio.getPermitirReservaCompleta())) {
            throw new ReglaNegocioException("NO_RESERVABLE", "El aula no admite reservas completas");
        }

        Usuario docente = usuarioRepository.findById(req.getDocenteAvalistaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", req.getDocenteAvalistaId()));
        if (docente.getTipoUsuario() != TipoUsuario.DOCENTE) {
            throw new ReglaNegocioException("AVALISTA_INVALIDO", "El avalista debe ser un docente");
        }

        if (haySolape(espacio.getId(), req.getFechaInicio(), req.getFechaFin())) {
            throw new ReglaNegocioException("CHOQUE_HORARIO", "El aula ya esta reservada en ese horario");
        }

        Reserva r = Reserva.builder()
                .solicitante(yo)
                .espacioFisico(espacio)
                .docenteAvalista(docente)
                .motivo(req.getMotivo())
                .fechaInicio(req.getFechaInicio())
                .fechaFin(req.getFechaFin())
                .estado(EstadoReserva.PENDIENTE_AVAL)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Reserva saved = repository.save(r);
        auditoriaService.registrar(AuditoriaService.CREA_RESERVA, "Reserva", String.valueOf(saved.getId()),
                AuditoriaService.OK, "Reserva de " + espacio.getCodigo() + " pendiente de aval");
        notificacionService.notificar(docente, NotificacionService.AVAL_RESERVA,
                "Nueva solicitud de aval de reserva",
                yo.getNombre() + " " + yo.getApellidos() + " solicita reservar " + espacio.getCodigo()
                        + ". Motivo: " + req.getMotivo());
        return toResponse(saved);
    }

    /** HU-14: el docente avalista aprueba o rechaza la reserva. */
    @Transactional
    public ReservaResponse avalar(Long id, AvalRequest req) {
        Usuario yo = currentUser.obtenerActual();
        Reserva r = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", id));

        boolean esAdmin = yo.getTipoUsuario() == TipoUsuario.ADMINISTRATIVO;
        if (!esAdmin && !r.getDocenteAvalista().getId().equals(yo.getId())) {
            throw new AccesoNoAutorizadoException("Solo el docente avalista puede resolver esta solicitud");
        }
        if (r.getEstado() != EstadoReserva.PENDIENTE_AVAL) {
            throw new ReglaNegocioException("ESTADO_INVALIDO", "La reserva no esta pendiente de aval");
        }

        r.setComentarioAval(req.getComentario());
        if (Boolean.TRUE.equals(req.getAprobar())) {
            if (haySolape(r.getEspacioFisico().getId(), r.getFechaInicio(), r.getFechaFin())) {
                throw new ReglaNegocioException("CHOQUE_HORARIO",
                        "Otra reserva aprobada choca con este horario");
            }
            r.setEstado(EstadoReserva.APROBADA);
            auditoriaService.registrar(AuditoriaService.AVALA_RESERVA, "Reserva", String.valueOf(id),
                    AuditoriaService.OK, "Reserva aprobada");
            notificacionService.notificar(r.getSolicitante(), NotificacionService.RESERVA_RESUELTA,
                    "Tu reserva fue APROBADA",
                    "Tu reserva de " + r.getEspacioFisico().getCodigo() + " fue aprobada por el docente.");
        } else {
            r.setEstado(EstadoReserva.RECHAZADA);
            auditoriaService.registrar(AuditoriaService.RECHAZA_RESERVA, "Reserva", String.valueOf(id),
                    AuditoriaService.OK, "Reserva rechazada");
            notificacionService.notificar(r.getSolicitante(), NotificacionService.RESERVA_RESUELTA,
                    "Tu reserva fue RECHAZADA",
                    "Tu reserva de " + r.getEspacioFisico().getCodigo() + " fue rechazada."
                            + (req.getComentario() != null ? " Motivo: " + req.getComentario() : ""));
        }
        return toResponse(repository.save(r));
    }

    @Transactional
    public ReservaResponse cancelar(Long id) {
        Usuario yo = currentUser.obtenerActual();
        Reserva r = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", id));
        if (!r.getSolicitante().getId().equals(yo.getId()) && yo.getTipoUsuario() != TipoUsuario.ADMINISTRATIVO) {
            throw new AccesoNoAutorizadoException("Solo el solicitante puede cancelar la reserva");
        }
        if (r.getEstado() == EstadoReserva.RECHAZADA || r.getEstado() == EstadoReserva.CANCELADA) {
            throw new ReglaNegocioException("ESTADO_INVALIDO", "La reserva ya esta finalizada");
        }
        r.setEstado(EstadoReserva.CANCELADA);
        return toResponse(repository.save(r));
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> misReservas() {
        Usuario yo = currentUser.obtenerActual();
        return repository.findBySolicitante_IdOrderByFechaInicioDesc(yo.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> bandejaDocente() {
        Usuario yo = currentUser.obtenerActual();
        if (yo.getTipoUsuario() != TipoUsuario.DOCENTE && yo.getTipoUsuario() != TipoUsuario.ADMINISTRATIVO) {
            throw new AccesoNoAutorizadoException("Solo docentes pueden ver la bandeja de avales");
        }
        return repository.findByDocenteAvalista_IdAndEstadoOrderByFechaInicioAsc(yo.getId(), EstadoReserva.PENDIENTE_AVAL)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> aprobadas() {
        return repository.findByEstadoOrderByFechaInicioAsc(EstadoReserva.APROBADA)
                .stream().map(this::toResponse).toList();
    }

    // --- internos ---

    private boolean haySolape(Long espacioId, LocalDateTime inicio, LocalDateTime fin) {
        return repository.existsByEspacioFisico_IdAndEstadoAndFechaInicioLessThanAndFechaFinGreaterThan(
                espacioId, EstadoReserva.APROBADA, fin, inicio);
    }

    private void validarFinAcademico(String motivo) {
        String m = motivo.toLowerCase();
        for (String kw : MOTIVOS_PROHIBIDOS) {
            if (m.contains(kw)) {
                auditoriaService.registrar(AuditoriaService.BLOQUEO_POR_REGLA, "Reserva", null,
                        AuditoriaService.DENEGADO, "Motivo no academico bloqueado (RN-06): term='" + kw + "'");
                throw new AccesoNoAutorizadoException(
                        "Motivo no permitido: la reserva parece tener fines no academicos (lucro, fiestas, "
                                + "proselitismo politico o cultos). RN-06.");
            }
        }
    }

    private ReservaResponse toResponse(Reserva r) {
        return ReservaResponse.builder()
                .id(r.getId())
                .espacioId(r.getEspacioFisico().getId())
                .espacioCodigo(r.getEspacioFisico().getCodigo())
                .solicitanteId(r.getSolicitante().getId())
                .solicitanteNombre(r.getSolicitante().getNombre() + " " + r.getSolicitante().getApellidos())
                .docenteAvalistaId(r.getDocenteAvalista().getId())
                .docenteAvalistaNombre(r.getDocenteAvalista().getNombre() + " " + r.getDocenteAvalista().getApellidos())
                .motivo(r.getMotivo())
                .fechaInicio(r.getFechaInicio())
                .fechaFin(r.getFechaFin())
                .estado(r.getEstado())
                .comentarioAval(r.getComentarioAval())
                .build();
    }
}
