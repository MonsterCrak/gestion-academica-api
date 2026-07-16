package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.AsignarSesionRequest;
import edu.upc.sistema.gestionacademicaapi.dto.CrearSesionTutoriaRequest;
import edu.upc.sistema.gestionacademicaapi.dto.DemandaMateriaResponse;
import edu.upc.sistema.gestionacademicaapi.dto.InscribirTutoriaRequest;
import edu.upc.sistema.gestionacademicaapi.dto.InscritoTutoriaResponse;
import edu.upc.sistema.gestionacademicaapi.dto.InscripcionTutoriaResponse;
import edu.upc.sistema.gestionacademicaapi.dto.SesionTutoriaResponse;
import edu.upc.sistema.gestionacademicaapi.entity.DemandaTutoria;
import edu.upc.sistema.gestionacademicaapi.entity.DocenteMateria;
import edu.upc.sistema.gestionacademicaapi.entity.EspacioFisico;
import edu.upc.sistema.gestionacademicaapi.entity.Materia;
import edu.upc.sistema.gestionacademicaapi.entity.SesionTutoria;
import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoSesionTutoria;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.AccesoNoAutorizadoException;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.exception.ReglaNegocioException;
import edu.upc.sistema.gestionacademicaapi.repository.DemandaTutoriaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.DocenteMateriaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.EspacioFisicoRepository;
import edu.upc.sistema.gestionacademicaapi.repository.MateriaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.SesionTutoriaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Tutorías por maduración de demanda (HU-16/17/18).
 * Al alcanzar el quórum de 5 inscritos en una asignatura se consolida automáticamente
 * la sesión, emparejando docente y aula disponibles; los excedentes van a sala de espera.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TutoriaService {

    private static final int QUORUM = 5;
    private static final String ENTIDAD_TUTORIA = "Tutoria";

    private final DemandaTutoriaRepository demandaRepository;
    private final SesionTutoriaRepository sesionRepository;
    private final MateriaRepository materiaRepository;
    private final DocenteMateriaRepository docenteMateriaRepository;
    private final EspacioFisicoRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CurrentUserService currentUser;
    private final PenalizacionService penalizacionService;
    private final AuditoriaService auditoriaService;
    private final NotificacionService notificacionService;

    /** HU-16: el alumno se inscribe en la demanda de una asignatura. */
    @Transactional
    public DemandaMateriaResponse inscribir(InscribirTutoriaRequest req) {
        Usuario yo = currentUser.obtenerActual();
        if (yo.getTipoUsuario() != TipoUsuario.ESTUDIANTE) {
            throw new ReglaNegocioException("TIPO_INVALIDO", "Solo estudiantes pueden inscribirse en tutorias");
        }
        penalizacionService.verificarPuedeOperar(yo, "Inscripcion en tutoria");

        Materia materia = materiaRepository.findById(req.getMateriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Materia", req.getMateriaId()));

        if (demandaRepository.existsByMateria_IdAndAlumno_IdAndSesionIsNull(materia.getId(), yo.getId())) {
            throw new ReglaNegocioException("YA_INSCRITO",
                    "Ya estas inscrito en la demanda de esta asignatura");
        }

        demandaRepository.save(DemandaTutoria.builder()
                .materia(materia)
                .alumno(yo)
                .fechaInscripcion(LocalDateTime.now())
                .enListaEspera(false)
                .build());

        auditoriaService.registrar(AuditoriaService.INSCRIBE_TUTORIA, ENTIDAD_TUTORIA,
                String.valueOf(materia.getId()), AuditoriaService.OK,
                "Inscripcion en demanda de " + materia.getCodigo());

        // HU-17: consolidación automática al alcanzar el quórum.
        long inscritos = demandaRepository.countByMateria_IdAndSesionIsNull(materia.getId());
        if (inscritos >= QUORUM) {
            consolidar(materia);
        }
        return demandaMateria(materia, yo.getId());
    }

    /**
     * Creación directa de una sesión de tutoría por un docente o administrador, con cupo
     * de participantes. Queda abierta a inscripción libre de alumnos hasta llenar el cupo,
     * en paralelo al flujo de maduración por quórum.
     */
    @Transactional
    public SesionTutoriaResponse crearSesion(CrearSesionTutoriaRequest req) {
        Usuario yo = currentUser.obtenerActual();
        if (yo.getTipoUsuario() != TipoUsuario.DOCENTE && yo.getTipoUsuario() != TipoUsuario.ADMINISTRATIVO) {
            throw new AccesoNoAutorizadoException("Solo docentes o administradores pueden crear tutorias");
        }

        Materia materia = materiaRepository.findById(req.getMateriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Materia", req.getMateriaId()));

        Usuario docente;
        if (yo.getTipoUsuario() == TipoUsuario.DOCENTE) {
            docente = yo;
        } else {
            if (req.getDocenteId() == null) {
                throw new ReglaNegocioException("DOCENTE_REQUERIDO", "Debe indicar el docente a cargo de la tutoria");
            }
            docente = usuarioRepository.findById(req.getDocenteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", req.getDocenteId()));
            if (docente.getTipoUsuario() != TipoUsuario.DOCENTE) {
                throw new ReglaNegocioException("DOCENTE_INVALIDO", "El usuario asignado debe ser docente");
            }
        }

        EspacioFisico aula = null;
        if (req.getAulaId() != null) {
            aula = espacioRepository.findById(req.getAulaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("EspacioFisico", req.getAulaId()));
        }

        int cupo = req.getCupo();
        if (aula != null && aula.getAforo() != null && cupo > aula.getAforo()) {
            cupo = aula.getAforo(); // el cupo no puede exceder el aforo del aula elegida
        }

        LocalDateTime inicio = req.getFechaHoraInicio();
        SesionTutoria sesion = sesionRepository.save(SesionTutoria.builder()
                .materia(materia)
                .docente(docente)
                .aula(aula)
                .fechaHoraInicio(inicio)
                .fechaHoraFin(inicio.plusHours(2))
                .estado(EstadoSesionTutoria.CONFIRMADA)
                .cupo(cupo)
                .abierta(true)
                .fechaCreacion(LocalDateTime.now())
                .build());

        auditoriaService.registrar(AuditoriaService.CONSOLIDA_TUTORIA, ENTIDAD_TUTORIA,
                String.valueOf(sesion.getId()), AuditoriaService.OK,
                "Sesion abierta creada por " + yo.getIdentificadorCorporativo() + " para "
                        + materia.getCodigo() + " (cupo " + cupo + ")");
        log.info("Sesion de tutoria abierta creada: materia={} docente={} cupo={}",
                materia.getCodigo(), docente.getIdentificadorCorporativo(), cupo);
        return sesionResponse(sesion, null);
    }

    /** El alumno se inscribe directamente en una sesión abierta, mientras haya cupo. */
    @Transactional
    public SesionTutoriaResponse inscribirEnSesion(Long sesionId) {
        Usuario yo = currentUser.obtenerActual();
        if (yo.getTipoUsuario() != TipoUsuario.ESTUDIANTE) {
            throw new ReglaNegocioException("TIPO_INVALIDO", "Solo estudiantes pueden inscribirse en tutorias");
        }
        penalizacionService.verificarPuedeOperar(yo, "Inscripcion en tutoria");

        SesionTutoria sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("SesionTutoria", sesionId));
        if (!Boolean.TRUE.equals(sesion.getAbierta()) || sesion.getEstado() != EstadoSesionTutoria.CONFIRMADA) {
            throw new ReglaNegocioException("NO_INSCRIBIBLE", "Esta sesion no admite inscripcion libre");
        }
        if (demandaRepository.existsBySesion_IdAndAlumno_Id(sesionId, yo.getId())) {
            throw new ReglaNegocioException("YA_INSCRITO", "Ya estas inscrito en esta tutoria");
        }
        long inscritos = demandaRepository.countBySesion_IdAndEnListaEsperaFalse(sesionId);
        if (sesion.getCupo() != null && inscritos >= sesion.getCupo()) {
            throw new ReglaNegocioException("CUPO_LLENO", "La tutoria ya alcanzo su cupo de participantes");
        }

        demandaRepository.save(DemandaTutoria.builder()
                .materia(sesion.getMateria())
                .alumno(yo)
                .sesion(sesion)
                .fechaInscripcion(LocalDateTime.now())
                .enListaEspera(false)
                .build());

        auditoriaService.registrar(AuditoriaService.INSCRIBE_TUTORIA, ENTIDAD_TUTORIA,
                String.valueOf(sesionId), AuditoriaService.OK,
                "Inscripcion en sesion abierta de " + sesion.getMateria().getCodigo());
        notificacionService.notificar(yo, NotificacionService.TUTORIA_CONFIRMADA,
                "Inscripcion confirmada en tutoria",
                "Te inscribiste en la tutoria de " + sesion.getMateria().getNombre() + ".");
        return sesionResponse(sesion, yo.getId());
    }

    /** HU-17: empareja docente + aula y confirma la sesión; excedentes a sala de espera (HU-18). */
    private void consolidar(Materia materia) {
        List<DemandaTutoria> pendientes =
                demandaRepository.findByMateria_IdAndSesionIsNullOrderByFechaInscripcionAsc(materia.getId());
        if (pendientes.size() < QUORUM) {
            return;
        }

        Usuario docente = docenteMateriaRepository.findByMateria_IdAndActivoTrue(materia.getId())
                .stream().map(DocenteMateria::getDocente).findFirst().orElse(null);

        EspacioFisico aula = espacioRepository.findByPermitirReservaCompletaTrue().stream()
                .filter(e -> Boolean.TRUE.equals(e.getActivo()) && e.getAforo() != null && e.getAforo() >= QUORUM)
                .min(Comparator.comparingInt(EspacioFisico::getAforo))
                .orElse(null);

        boolean completo = docente != null && aula != null;
        LocalDateTime inicio = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0).withSecond(0).withNano(0);

        SesionTutoria sesion = sesionRepository.save(SesionTutoria.builder()
                .materia(materia)
                .docente(docente)
                .aula(aula)
                .fechaHoraInicio(completo ? inicio : null)
                .fechaHoraFin(completo ? inicio.plusHours(2) : null)
                .estado(completo ? EstadoSesionTutoria.CONFIRMADA : EstadoSesionTutoria.EN_ESPERA_RECURSOS)
                .cupo(aula != null ? aula.getAforo() : null)
                .fechaCreacion(LocalDateTime.now())
                .build());

        int cupo = aula != null ? aula.getAforo() : pendientes.size();
        int idx = 0;
        for (DemandaTutoria d : pendientes) {
            boolean espera = idx >= cupo;
            d.setSesion(sesion);
            d.setEnListaEspera(espera);
            demandaRepository.save(d);
            notificarInscrito(d.getAlumno(), materia, completo, espera, inicio);
            idx++;
        }

        auditoriaService.registrarComo(null, AuditoriaService.CONSOLIDA_TUTORIA, ENTIDAD_TUTORIA,
                String.valueOf(sesion.getId()), AuditoriaService.OK,
                "Consolidada " + materia.getCodigo() + " con " + pendientes.size() + " inscritos"
                        + (completo ? " (docente y aula asignados)" : " (EN ESPERA de recursos)"));
        log.info("Tutoria consolidada: materia={} inscritos={} estado={}",
                materia.getCodigo(), pendientes.size(), sesion.getEstado());
    }

    /** El alumno cancela su inscripción pendiente en la demanda de una asignatura (antes de consolidarse). */
    @Transactional
    public void cancelarDemanda(Long materiaId) {
        Usuario yo = currentUser.obtenerActual();
        List<DemandaTutoria> pendientes =
                demandaRepository.findByMateria_IdAndAlumno_IdAndSesionIsNull(materiaId, yo.getId());
        if (pendientes.isEmpty()) {
            throw new ReglaNegocioException("NO_INSCRITO", "No tienes una inscripcion pendiente en esta asignatura");
        }
        demandaRepository.deleteAll(pendientes);
        auditoriaService.registrar(AuditoriaService.INSCRIBE_TUTORIA, ENTIDAD_TUTORIA, String.valueOf(materiaId),
                AuditoriaService.OK, "Cancelacion de inscripcion en demanda de tutoria");
    }

    /** El alumno se retira de una sesión abierta (antes de que se realice). */
    @Transactional
    public void cancelarInscripcionSesion(Long sesionId) {
        Usuario yo = currentUser.obtenerActual();
        SesionTutoria sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("SesionTutoria", sesionId));
        if (sesion.getEstado() == EstadoSesionTutoria.REALIZADA) {
            throw new ReglaNegocioException("ESTADO_INVALIDO", "La tutoria ya se realizo");
        }
        List<DemandaTutoria> mias = demandaRepository.findBySesion_IdAndAlumno_Id(sesionId, yo.getId());
        if (mias.isEmpty()) {
            throw new ReglaNegocioException("NO_INSCRITO", "No estas inscrito en esta tutoria");
        }
        demandaRepository.deleteAll(mias);
        auditoriaService.registrar(AuditoriaService.INSCRIBE_TUTORIA, ENTIDAD_TUTORIA, String.valueOf(sesionId),
                AuditoriaService.OK, "Cancelacion de inscripcion en sesion de tutoria");
    }

    /** El administrador da de baja una sesión de tutoría (queda CANCELADA para todos). */
    @Transactional
    public SesionTutoriaResponse cancelarSesion(Long sesionId) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        SesionTutoria sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("SesionTutoria", sesionId));
        if (sesion.getEstado() == EstadoSesionTutoria.REALIZADA || sesion.getEstado() == EstadoSesionTutoria.CANCELADA) {
            throw new ReglaNegocioException("ESTADO_INVALIDO", "La sesion ya esta finalizada");
        }
        sesion.setEstado(EstadoSesionTutoria.CANCELADA);
        sesion.setAbierta(false);
        SesionTutoria saved = sesionRepository.save(sesion);
        demandaRepository.findBySesion_Id(sesionId).forEach(d ->
                notificacionService.notificar(d.getAlumno(), NotificacionService.RESERVA_RESUELTA,
                        "Tutoria cancelada",
                        "La tutoria de " + sesion.getMateria().getNombre() + " fue dada de baja por la administracion."));
        auditoriaService.registrar(AuditoriaService.CONSOLIDA_TUTORIA, ENTIDAD_TUTORIA, String.valueOf(sesionId),
                AuditoriaService.OK, "Sesion de tutoria dada de baja por administrador");
        return sesionResponse(saved, null);
    }

    /** HU-18/CP-15: el coordinador asigna manualmente docente y aula a una sesión en espera. */
    @Transactional
    public SesionTutoriaResponse asignarRecursos(Long sesionId, AsignarSesionRequest req) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        SesionTutoria sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("SesionTutoria", sesionId));
        if (sesion.getEstado() != EstadoSesionTutoria.EN_ESPERA_RECURSOS) {
            throw new ReglaNegocioException("ESTADO_INVALIDO", "La sesion no esta en espera de recursos");
        }
        Usuario docente = usuarioRepository.findById(req.getDocenteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", req.getDocenteId()));
        if (docente.getTipoUsuario() != TipoUsuario.DOCENTE) {
            throw new ReglaNegocioException("DOCENTE_INVALIDO", "El usuario asignado debe ser docente");
        }
        EspacioFisico aula = espacioRepository.findById(req.getAulaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("EspacioFisico", req.getAulaId()));

        LocalDateTime inicio = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0).withSecond(0).withNano(0);
        sesion.setDocente(docente);
        sesion.setAula(aula);
        sesion.setCupo(aula.getAforo());
        sesion.setFechaHoraInicio(inicio);
        sesion.setFechaHoraFin(inicio.plusHours(2));
        sesion.setEstado(EstadoSesionTutoria.CONFIRMADA);
        SesionTutoria saved = sesionRepository.save(sesion);

        // Recalcula sala de espera según el aforo asignado y notifica.
        List<DemandaTutoria> inscritos = demandaRepository.findBySesion_Id(sesionId);
        int idx = 0;
        for (DemandaTutoria d : inscritos) {
            boolean espera = idx >= aula.getAforo();
            d.setEnListaEspera(espera);
            demandaRepository.save(d);
            notificarInscrito(d.getAlumno(), sesion.getMateria(), true, espera, inicio);
            idx++;
        }
        auditoriaService.registrar(AuditoriaService.CONSOLIDA_TUTORIA, ENTIDAD_TUTORIA, String.valueOf(sesionId),
                AuditoriaService.OK, "Asignacion manual de docente y aula");
        return sesionResponse(saved, null);
    }

    @Transactional(readOnly = true)
    public List<DemandaMateriaResponse> listarDemanda() {
        Usuario yo = currentUser.obtenerActual();
        return materiaRepository.findAll().stream()
                .map(m -> demandaMateria(m, yo.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InscripcionTutoriaResponse> misInscripciones() {
        Usuario yo = currentUser.obtenerActual();
        return demandaRepository.findByAlumno_IdOrderByFechaInscripcionDesc(yo.getId())
                .stream().map(this::inscripcionResponse).toList();
    }

    /** Lista los alumnos inscritos en una sesión. Solo el administrador o el docente a cargo. */
    @Transactional(readOnly = true)
    public List<InscritoTutoriaResponse> listarInscritos(Long sesionId) {
        Usuario yo = currentUser.obtenerActual();
        SesionTutoria sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("SesionTutoria", sesionId));
        boolean esAdmin = yo.getTipoUsuario() == TipoUsuario.ADMINISTRATIVO;
        boolean esDocenteACargo = sesion.getDocente() != null && sesion.getDocente().getId().equals(yo.getId());
        if (!esAdmin && !esDocenteACargo) {
            throw new AccesoNoAutorizadoException("Solo el docente a cargo o un administrador pueden ver los inscritos");
        }
        return demandaRepository.findBySesion_Id(sesionId).stream()
                .sorted((a, b) -> {
                    int cmp = Boolean.compare(Boolean.TRUE.equals(a.getEnListaEspera()), Boolean.TRUE.equals(b.getEnListaEspera()));
                    if (cmp != 0) {
                        return cmp; // primero los confirmados, luego los de sala de espera
                    }
                    return a.getFechaInscripcion().compareTo(b.getFechaInscripcion());
                })
                .map(d -> {
                    Usuario a = d.getAlumno();
                    return InscritoTutoriaResponse.builder()
                            .alumnoId(a.getId())
                            .identificadorCorporativo(a.getIdentificadorCorporativo())
                            .nombre(a.getNombre())
                            .apellidos(a.getApellidos())
                            .email(a.getEmail())
                            .fechaInscripcion(d.getFechaInscripcion())
                            .enListaEspera(Boolean.TRUE.equals(d.getEnListaEspera()))
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SesionTutoriaResponse> listarSesiones() {
        Usuario yo = currentUser.obtenerActual();
        List<SesionTutoria> sesiones;
        Long alumnoId = null;
        if (yo.getTipoUsuario() == TipoUsuario.ADMINISTRATIVO) {
            sesiones = sesionRepository.findAllByOrderByFechaCreacionDesc();
        } else if (yo.getTipoUsuario() == TipoUsuario.DOCENTE) {
            sesiones = sesionRepository.findByDocente_IdOrderByFechaHoraInicioAsc(yo.getId());
        } else {
            alumnoId = yo.getId();
            // El alumno ve las sesiones abiertas a inscripción libre + aquellas en las que ya participa.
            LinkedHashMap<Long, SesionTutoria> unicas = new LinkedHashMap<>();
            sesionRepository.findByAbiertaTrueAndEstadoOrderByFechaHoraInicioAsc(EstadoSesionTutoria.CONFIRMADA)
                    .forEach(s -> unicas.put(s.getId(), s));
            demandaRepository.findByAlumno_IdOrderByFechaInscripcionDesc(yo.getId()).stream()
                    .map(DemandaTutoria::getSesion).filter(Objects::nonNull)
                    .forEach(s -> unicas.put(s.getId(), s));
            sesiones = new ArrayList<>(unicas.values());
        }
        final Long aid = alumnoId;
        return sesiones.stream().map(s -> sesionResponse(s, aid)).toList();
    }

    // --- internos ---

    private void notificarInscrito(Usuario alumno, Materia materia, boolean completo, boolean espera, LocalDateTime inicio) {
        String tipo = (completo && !espera) ? NotificacionService.TUTORIA_CONFIRMADA
                : NotificacionService.TUTORIA_LISTA_ESPERA;
        String asunto;
        String cuerpo;
        if (!completo) {
            asunto = "Tutoria en espera de asignacion";
            cuerpo = "Se alcanzo el quorum en " + materia.getNombre() + ". La sesion espera asignacion de docente y aula.";
        } else if (espera) {
            asunto = "Estas en la lista de espera de la tutoria";
            cuerpo = "Se confirmo la sesion de " + materia.getNombre() + " pero se supero el aforo; quedas en sala de espera priorizada.";
        } else {
            asunto = "Tu tutoria fue confirmada";
            cuerpo = "Sesion de " + materia.getNombre() + " confirmada para el " + inicio + ".";
        }
        notificacionService.notificar(alumno, tipo, asunto, cuerpo);
    }

    private DemandaMateriaResponse demandaMateria(Materia m, Long alumnoId) {
        return DemandaMateriaResponse.builder()
                .materiaId(m.getId())
                .codigo(m.getCodigo())
                .nombre(m.getNombre())
                .departamento(m.getDepartamento())
                .inscritos(demandaRepository.countByMateria_IdAndSesionIsNull(m.getId()))
                .quorum(QUORUM)
                .yaInscrito(demandaRepository.existsByMateria_IdAndAlumno_IdAndSesionIsNull(m.getId(), alumnoId))
                .build();
    }

    private InscripcionTutoriaResponse inscripcionResponse(DemandaTutoria d) {
        SesionTutoria s = d.getSesion();
        return InscripcionTutoriaResponse.builder()
                .id(d.getId())
                .materiaCodigo(d.getMateria().getCodigo())
                .materiaNombre(d.getMateria().getNombre())
                .fechaInscripcion(d.getFechaInscripcion())
                .sesionId(s != null ? s.getId() : null)
                .estado(s != null ? s.getEstado().name() : "PENDIENTE_QUORUM")
                .enListaEspera(d.getEnListaEspera())
                .fechaHoraInicio(s != null ? s.getFechaHoraInicio() : null)
                .build();
    }

    private SesionTutoriaResponse sesionResponse(SesionTutoria s, Long alumnoId) {
        return SesionTutoriaResponse.builder()
                .id(s.getId())
                .materiaCodigo(s.getMateria().getCodigo())
                .materiaNombre(s.getMateria().getNombre())
                .docenteNombre(s.getDocente() != null
                        ? s.getDocente().getNombre() + " " + s.getDocente().getApellidos() : null)
                .aulaCodigo(s.getAula() != null ? s.getAula().getCodigo() : null)
                .cupo(s.getCupo())
                .fechaHoraInicio(s.getFechaHoraInicio())
                .fechaHoraFin(s.getFechaHoraFin())
                .estado(s.getEstado())
                .inscritos(demandaRepository.countBySesion_IdAndEnListaEsperaFalse(s.getId()))
                .enListaEspera(demandaRepository.countBySesion_IdAndEnListaEsperaTrue(s.getId()))
                .abierta(Boolean.TRUE.equals(s.getAbierta()))
                .yaInscrito(alumnoId != null
                        && demandaRepository.existsBySesion_IdAndAlumno_Id(s.getId(), alumnoId))
                .build();
    }
}
