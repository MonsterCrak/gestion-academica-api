package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.ApelarRequest;
import edu.upc.sistema.gestionacademicaapi.dto.AsignarDocenteRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ConfirmacionAlumnoResponse;
import edu.upc.sistema.gestionacademicaapi.dto.ConfirmarAsistenciaRequest;
import edu.upc.sistema.gestionacademicaapi.dto.ResolverRevisionRequest;
import edu.upc.sistema.gestionacademicaapi.dto.SolicitudTutoriaCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.SolicitudTutoriaResponse;
import edu.upc.sistema.gestionacademicaapi.entity.ConfirmacionTutoriaAlumno;
import edu.upc.sistema.gestionacademicaapi.entity.DocenteMateria;
import edu.upc.sistema.gestionacademicaapi.entity.EspacioFisico;
import edu.upc.sistema.gestionacademicaapi.entity.Materia;
import edu.upc.sistema.gestionacademicaapi.entity.RegistroHorasTutoria;
import edu.upc.sistema.gestionacademicaapi.entity.SolicitudTutoria;
import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.enums.CerradoPor;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoConfirmacion;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoSolicitud;
import edu.upc.sistema.gestionacademicaapi.enums.ModalidadAsignacion;
import edu.upc.sistema.gestionacademicaapi.enums.TipoBloqueo;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.AccesoNoAutorizadoException;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.exception.ReglaNegocioException;
import edu.upc.sistema.gestionacademicaapi.repository.BloqueHorarioRepository;
import edu.upc.sistema.gestionacademicaapi.repository.ConfirmacionTutoriaAlumnoRepository;
import edu.upc.sistema.gestionacademicaapi.repository.DocenteMateriaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.EspacioFisicoRepository;
import edu.upc.sistema.gestionacademicaapi.repository.MateriaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.RegistroHorasTutoriaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.SolicitudTutoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitudTutoriaService {

    private static final int QUORUM_REQUERIDO = 5;
    private static final int PESO_VOTO_DOCENTE = 3;
    private static final double UMBRAL_PORCENTAJE = 0.65;
    private static final Duration VENTANA_DOCENTE = Duration.ofHours(24);
    private static final Duration VENTANA_ALUMNO = Duration.ofDays(7);

    private final SolicitudTutoriaRepository solicitudRepository;
    private final ConfirmacionTutoriaAlumnoRepository confirmacionRepository;
    private final DocenteMateriaRepository docenteMateriaRepository;
    private final EspacioFisicoRepository espacioRepository;
    private final MateriaRepository materiaRepository;
    private final BloqueHorarioRepository bloqueHorarioRepository;
    private final RegistroHorasTutoriaRepository registroHorasRepository;
    private final CurrentUserService currentUser;

    @Transactional
    public SolicitudTutoriaResponse crear(SolicitudTutoriaCreateRequest req) {
        Usuario yo = currentUser.obtenerActual();
        if (yo.getTipoUsuario() != TipoUsuario.ESTUDIANTE) {
            throw new ReglaNegocioException("TIPO_INVALIDO", "Solo estudiantes pueden crear solicitudes");
        }
        if (req.getDuracionHoras().compareTo(new BigDecimal("1.00")) < 0
                || req.getDuracionHoras().compareTo(new BigDecimal("2.00")) > 0) {
            throw new ReglaNegocioException("RN-14", "duracion debe estar entre 1 y 2 horas");
        }
        Materia materia = materiaRepository.findById(req.getMateriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Materia", req.getMateriaId()));

        SolicitudTutoria s = SolicitudTutoria.builder()
                .creador(yo)
                .materia(materia)
                .espacioAsignado(null)
                .docenteAsignado(null)
                .tipoAulaSolicitada(req.getTipoAulaSolicitada())
                .fechaHoraInicio(req.getFechaHoraInicio())
                .fechaHoraFin(req.getFechaHoraInicio().plusHours(req.getDuracionHoras().longValue()))
                .duracionHoras(req.getDuracionHoras().setScale(2, RoundingMode.HALF_UP))
                .tokenInvitacion(UUID.randomUUID().toString())
                .fechaExpiracionToken(req.getFechaHoraInicio().minusHours(1))
                .estado(EstadoSolicitud.PENDIENTE_QUORUM)
                .docenteConfirmoRealizacion(false)
                .totalConfirmados(0)
                .build();
        return toResponse(solicitudRepository.save(s));
    }

    @Transactional(readOnly = true)
    public SolicitudTutoriaResponse obtener(Long id) {
        return toResponse(buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public List<ConfirmacionAlumnoResponse> listarConfirmaciones(Long solicitudId) {
        buscarPorId(solicitudId);
        return confirmacionRepository.findBySolicitud_Id(solicitudId).stream()
                .map(this::toConfirmacionResponse).toList();
    }

    @Transactional
    public ConfirmacionAlumnoResponse aceptarORechazarInvitacion(String token, boolean acepta) {
        SolicitudTutoria s = solicitudRepository.findByTokenInvitacion(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("SolicitudTutoria", "token=" + token));
        if (s.getEstado() != EstadoSolicitud.PENDIENTE_QUORUM) {
            throw new ReglaNegocioException("ESTADO_INVALIDO",
                    "La solicitud ya no acepta invitaciones (estado=" + s.getEstado() + ")");
        }
        Usuario alumno = currentUser.obtenerActual();
        if (alumno.getTipoUsuario() != TipoUsuario.ESTUDIANTE) {
            throw new ReglaNegocioException("TIPO_INVALIDO", "Solo estudiantes pueden aceptar invitaciones");
        }
        Optional<ConfirmacionTutoriaAlumno> existente =
                confirmacionRepository.findBySolicitud_IdAndAlumno_Id(s.getId(), alumno.getId());

        ConfirmacionTutoriaAlumno c = existente.orElseGet(() ->
                ConfirmacionTutoriaAlumno.builder()
                        .solicitud(s)
                        .alumno(alumno)
                        .apelo(false)
                        .build());
        c.setEstadoConfirmacion(acepta ? EstadoConfirmacion.ACEPTADA : EstadoConfirmacion.RECHAZADA);
        c.setFechaConfirmacion(LocalDateTime.now());
        ConfirmacionTutoriaAlumno guardada = confirmacionRepository.save(c);

        reevaluarQuorum(s);
        return toConfirmacionResponse(guardada);
    }

    @Transactional
    public void asignarDocente(Long solicitudId, AsignarDocenteRequest req) {
        Usuario yo = currentUser.obtenerActual();
        if (yo.getTipoUsuario() != TipoUsuario.DOCENTE
                && yo.getTipoUsuario() != TipoUsuario.ADMINISTRATIVO) {
            throw new AccesoNoAutorizadoException("Solo docentes o administrativos pueden asignar manualmente");
        }
        SolicitudTutoria s = buscarPorId(solicitudId);
        if (s.getEstado() != EstadoSolicitud.QUORUM_ALCANZADO) {
            throw new ReglaNegocioException("ESTADO_INVALIDO",
                    "Solo se asigna docente cuando hay quorum (estado actual=" + s.getEstado() + ")");
        }
        Usuario docente = currentUser.obtenerActual();
        if (!docente.getId().equals(req.getDocenteId())
                && yo.getTipoUsuario() != TipoUsuario.ADMINISTRATIVO) {
            throw new AccesoNoAutorizadoException("Solo puede asignarse a si mismo o un admin hacerlo");
        }
        boolean existe = docenteMateriaRepository
                .findByDocente_IdAndMateria_IdAndActivoTrue(docente.getId(), s.getMateria().getId())
                .isPresent();
        if (!existe) {
            throw new ReglaNegocioException("RN-07", "El docente no dicta esta materia");
        }
        s.setDocenteAsignado(docente);
        s.setEstado(EstadoSolicitud.CONFIRMADA);
        solicitudRepository.save(s);
    }

    @Transactional
    public SolicitudTutoriaResponse confirmarRealizacion(Long solicitudId) {
        Usuario docente = currentUser.obtenerActual();
        SolicitudTutoria s = buscarPorId(solicitudId);
        if (s.getDocenteAsignado() == null || !s.getDocenteAsignado().getId().equals(docente.getId())) {
            throw new AccesoNoAutorizadoException("Solo el docente asignado puede confirmar");
        }
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limiteDocente = s.getFechaHoraFin().plus(VENTANA_DOCENTE);
        if (ahora.isBefore(s.getFechaHoraInicio()) || ahora.isAfter(limiteDocente)) {
            throw new ReglaNegocioException("RN-17/RN-19",
                    "Fuera de la ventana del docente (inicio=" + s.getFechaHoraInicio()
                            + ", limite=" + limiteDocente + ")");
        }
        long aceptados = confirmacionRepository
                .countBySolicitud_IdAndEstadoConfirmacion(s.getId(), EstadoConfirmacion.ACEPTADA);
        s.setTotalConfirmados((int) aceptados);

        RegistroHorasTutoria reg = RegistroHorasTutoria.builder()
                .docente(docente)
                .materia(s.getMateria())
                .solicitud(s)
                .fechaHoraInicioReal(s.getFechaHoraInicio())
                .fechaHoraFinReal(s.getFechaHoraFin())
                .horasEfectivas(s.getDuracionHoras())
                .cerradoPor(CerradoPor.DOCENTE)
                .cerradoEn(ahora)
                .anulado(false)
                .build();
        registroHorasRepository.save(reg);

        s.setDocenteConfirmoRealizacion(true);
        s.setDocenteConfirmoRealizacionEn(ahora);
        s.setEstado(EstadoSolicitud.REALIZADA);
        return toResponse(solicitudRepository.save(s));
    }

    @Transactional
    public ConfirmacionAlumnoResponse marcarAsistencia(Long solicitudId, ConfirmarAsistenciaRequest req) {
        Usuario alumno = currentUser.obtenerActual();
        SolicitudTutoria s = buscarPorId(solicitudId);
        verificarVentanaAlumno(s, LocalDateTime.now());

        ConfirmacionTutoriaAlumno c = confirmacionRepository
                .findBySolicitud_IdAndAlumno_Id(s.getId(), alumno.getId())
                .orElseThrow(() -> new ReglaNegocioException("NO_INVITADO",
                        "El alumno no esta invitado a esta tutoria"));

        c.setAsistio(req.getAsistio());
        c.setFechaRegistroAsistencia(LocalDateTime.now());
        return toConfirmacionResponse(confirmacionRepository.save(c));
    }

    @Transactional
    public ConfirmacionAlumnoResponse apelar(Long solicitudId, ApelarRequest req) {
        Usuario alumno = currentUser.obtenerActual();
        SolicitudTutoria s = buscarPorId(solicitudId);
        verificarVentanaAlumno(s, LocalDateTime.now());

        ConfirmacionTutoriaAlumno c = confirmacionRepository
                .findBySolicitud_IdAndAlumno_Id(s.getId(), alumno.getId())
                .orElseThrow(() -> new ReglaNegocioException("NO_INVITADO",
                        "El alumno no esta invitado a esta tutoria"));

        if (Boolean.TRUE.equals(c.getAsistio())) {
            throw new ReglaNegocioException("RN-18",
                    "No se puede apelar si marco asistencia=TRUE");
        }
        if (Boolean.TRUE.equals(c.getApelo())) {
            throw new ReglaNegocioException("APELACION_DUPLICADA", "Ya apelo esta tutoria");
        }
        c.setApelo(true);
        c.setMotivoApelacion(req.getMotivo());
        c.setFechaApelacion(LocalDateTime.now());
        return toConfirmacionResponse(confirmacionRepository.save(c));
    }

    @Transactional
    public ConfirmacionAlumnoResponse retirarApelacion(Long solicitudId) {
        Usuario alumno = currentUser.obtenerActual();
        SolicitudTutoria s = buscarPorId(solicitudId);
        verificarVentanaAlumno(s, LocalDateTime.now());

        ConfirmacionTutoriaAlumno c = confirmacionRepository
                .findBySolicitud_IdAndAlumno_Id(s.getId(), alumno.getId())
                .orElseThrow(() -> new ReglaNegocioException("NO_INVITADO",
                        "El alumno no esta invitado a esta tutoria"));

        if (!Boolean.TRUE.equals(c.getApelo())) {
            throw new ReglaNegocioException("NO_APELO", "No existe una apelacion activa para retirar");
        }
        c.setApelo(false);
        c.setMotivoApelacion(null);
        c.setFechaApelacion(LocalDateTime.now());
        return toConfirmacionResponse(confirmacionRepository.save(c));
    }

    @Transactional
    public SolicitudTutoriaResponse resolverRevision(Long solicitudId, ResolverRevisionRequest req) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        SolicitudTutoria s = buscarPorId(solicitudId);
        if (s.getEstado() != EstadoSolicitud.REALIZADA_EN_REVISION) {
            throw new ReglaNegocioException("ESTADO_INVALIDO",
                    "La solicitud no esta en revision (estado=" + s.getEstado() + ")");
        }
        List<RegistroHorasTutoria> registros = registroHorasRepository.findBySolicitud_Id(s.getId());
        RegistroHorasTutoria reg = registros.isEmpty() ? null : registros.get(0);

        switch (req.getDecision()) {
            case RATIFICAR -> s.setEstado(EstadoSolicitud.REALIZADA_RATIFICADA);
            case REVOCAR -> {
                s.setEstado(EstadoSolicitud.REALIZADA_REVOCADA);
                if (reg != null) {
                    reg.setAnulado(true);
                    reg.setAnuladoPor(currentUser.obtenerActual());
                    reg.setAnuladoEn(LocalDateTime.now());
                    reg.setAnuladoMotivo(req.getMotivo());
                    registroHorasRepository.save(reg);
                }
            }
            case RECTIFICAR -> {
                if (req.getHorasRectificadas() == null) {
                    throw new ReglaNegocioException("HORAS_REQUERIDAS",
                            "Debe indicar horasRectificadas");
                }
                if (reg != null) {
                    reg.setHorasRectificadas(req.getHorasRectificadas());
                    reg.setRectificadoPor(currentUser.obtenerActual());
                    reg.setRectificadoEn(LocalDateTime.now());
                    reg.setRectificadoMotivo(req.getMotivo());
                    registroHorasRepository.save(reg);
                }
                s.setEstado(EstadoSolicitud.REALIZADA_RATIFICADA);
            }
        }
        return toResponse(solicitudRepository.save(s));
    }

    @Transactional(readOnly = true)
    public List<SolicitudTutoriaResponse> listar() {
        return solicitudRepository.findAll().stream().map(this::toResponse).toList();
    }

    void reevaluarQuorum(SolicitudTutoria s) {
        long aceptados = confirmacionRepository
                .countBySolicitud_IdAndEstadoConfirmacion(s.getId(), EstadoConfirmacion.ACEPTADA);
        if (s.getEstado() == EstadoSolicitud.PENDIENTE_QUORUM && aceptados >= QUORUM_REQUERIDO) {
            List<DocenteMateria> docentesHabilitados = docenteMateriaRepository
                    .findByMateria_IdAndTipoAulaRequeridaAndActivoTrue(
                            s.getMateria().getId(), s.getTipoAulaSolicitada());
            if (docentesHabilitados.isEmpty()) {
                throw new ReglaNegocioException("RN-07",
                        "No hay docentes activos que dicten esta materia con el tipo de aula solicitado");
            }
            DocenteMateria dmElegido = docentesHabilitados.stream()
                    .filter(d -> d.getModalidadAsignacion() == ModalidadAsignacion.AUTOMATICA)
                    .min(Comparator.comparing(d -> d.getDocente().getId()))
                    .orElse(docentesHabilitados.get(0));

            EspacioFisico aula = espacioRepository.findByPermitirReservaCompletaTrueAndTipoEspacio(
                            mapearAEnum(s.getTipoAulaSolicitada())).stream()
                    .filter(e -> e.getAforo() >= QUORUM_REQUERIDO)
                    .filter(e -> aulaDisponible(e, s.getFechaHoraInicio(), s.getFechaHoraFin()))
                    .findFirst()
                    .orElseThrow(() -> new ReglaNegocioException("SIN_AULA",
                            "No hay aula disponible del tipo " + s.getTipoAulaSolicitada()
                                    + " con aforo >= " + QUORUM_REQUERIDO + " libre de bloqueos"));

            s.setEspacioAsignado(aula);
            s.setDocenteAsignado(dmElegido.getDocente());
            s.setEstado(EstadoSolicitud.CONFIRMADA);
            if (dmElegido.getModalidadAsignacion() == ModalidadAsignacion.MANUAL) {
                s.setEstado(EstadoSolicitud.QUORUM_ALCANZADO);
            }
            solicitudRepository.save(s);
        }
    }

    private edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio mapearAEnum(
            edu.upc.sistema.gestionacademicaapi.enums.TipoAula tipoAula) {
        return switch (tipoAula) {
            case AULA_NORMAL -> edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio.AULA_NORMAL;
            case COMPUTO -> edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio.COMPUTO;
            case LABORATORIO -> edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio.LABORATORIO;
        };
    }

    private boolean aulaDisponible(EspacioFisico aula, LocalDateTime inicio, LocalDateTime fin) {
        return bloqueHorarioRepository
                .findByEspacioFisico_IdAndTipoBloqueoAndActivoTrue(aula.getId(), TipoBloqueo.DISPONIBILIDAD_AULA)
                .stream().noneMatch(b -> {
                    java.time.LocalDate fecha = inicio.toLocalDate();
                    if (fecha.isBefore(b.getFechaDesde())) return false;
                    if (b.getFechaHasta() != null && fecha.isAfter(b.getFechaHasta())) return false;
                    return fin.toLocalTime().compareTo(b.getHoraInicio()) > 0
                            && inicio.toLocalTime().compareTo(b.getHoraFin()) < 0;
                });
    }

    private void verificarVentanaAlumno(SolicitudTutoria s, LocalDateTime ahora) {
        LocalDateTime limiteAlumno = s.getFechaHoraFin().plus(VENTANA_ALUMNO);
        if (ahora.isBefore(s.getFechaHoraFin()) || ahora.isAfter(limiteAlumno)) {
            throw new ReglaNegocioException("RN-18/RN-20",
                    "Fuera de la ventana del alumno (limite=" + limiteAlumno + ")");
        }
    }

    public long contarApelantes(SolicitudTutoria s) {
        return confirmacionRepository.countBySolicitud_IdAndApeloTrue(s.getId());
    }

    public boolean superaUmbral(SolicitudTutoria s) {
        long apelantes = contarApelantes(s);
        long total = s.getTotalConfirmados() != null ? s.getTotalConfirmados() : 0L;
        if (apelantes > PESO_VOTO_DOCENTE) return true;
        if (total <= 0) return false;
        double ratio = (double) apelantes / total;
        return ratio >= UMBRAL_PORCENTAJE;
    }

    private SolicitudTutoria buscarPorId(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("SolicitudTutoria", id));
    }

    private SolicitudTutoriaResponse toResponse(SolicitudTutoria s) {
        return SolicitudTutoriaResponse.builder()
                .id(s.getId())
                .creadorId(s.getCreador() != null ? s.getCreador().getId() : null)
                .materiaId(s.getMateria() != null ? s.getMateria().getId() : null)
                .espacioAsignadoId(s.getEspacioAsignado() != null ? s.getEspacioAsignado().getId() : null)
                .docenteAsignadoId(s.getDocenteAsignado() != null ? s.getDocenteAsignado().getId() : null)
                .tipoAulaSolicitada(s.getTipoAulaSolicitada())
                .fechaHoraInicio(s.getFechaHoraInicio())
                .fechaHoraFin(s.getFechaHoraFin())
                .duracionHoras(s.getDuracionHoras())
                .tokenInvitacion(s.getTokenInvitacion())
                .fechaExpiracionToken(s.getFechaExpiracionToken())
                .estado(s.getEstado())
                .docenteConfirmoRealizacion(s.getDocenteConfirmoRealizacion())
                .docenteConfirmoRealizacionEn(s.getDocenteConfirmoRealizacionEn())
                .totalConfirmados(s.getTotalConfirmados())
                .build();
    }

    private ConfirmacionAlumnoResponse toConfirmacionResponse(ConfirmacionTutoriaAlumno c) {
        return ConfirmacionAlumnoResponse.builder()
                .id(c.getId())
                .solicitudId(c.getSolicitud().getId())
                .alumnoId(c.getAlumno().getId())
                .identificadorAlumno(c.getAlumno().getIdentificadorCorporativo())
                .nombreAlumno(c.getAlumno().getNombre() + " " + c.getAlumno().getApellidos())
                .fechaConfirmacion(c.getFechaConfirmacion())
                .estadoConfirmacion(c.getEstadoConfirmacion())
                .asistio(c.getAsistio())
                .fechaRegistroAsistencia(c.getFechaRegistroAsistencia())
                .apelo(c.getApelo())
                .motivoApelacion(c.getMotivoApelacion())
                .fechaApelacion(c.getFechaApelacion())
                .build();
    }
}
