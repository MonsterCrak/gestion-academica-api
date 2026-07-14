package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.AsignarSesionRequest;
import edu.upc.sistema.gestionacademicaapi.dto.DemandaMateriaResponse;
import edu.upc.sistema.gestionacademicaapi.dto.InscribirTutoriaRequest;
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
import java.util.Comparator;
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

        auditoriaService.registrar(AuditoriaService.INSCRIBE_TUTORIA, "Tutoria",
                String.valueOf(materia.getId()), AuditoriaService.OK,
                "Inscripcion en demanda de " + materia.getCodigo());

        // HU-17: consolidación automática al alcanzar el quórum.
        long inscritos = demandaRepository.countByMateria_IdAndSesionIsNull(materia.getId());
        if (inscritos >= QUORUM) {
            consolidar(materia);
        }
        return demandaMateria(materia, yo.getId());
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

        auditoriaService.registrarComo(null, AuditoriaService.CONSOLIDA_TUTORIA, "Tutoria",
                String.valueOf(sesion.getId()), AuditoriaService.OK,
                "Consolidada " + materia.getCodigo() + " con " + pendientes.size() + " inscritos"
                        + (completo ? " (docente y aula asignados)" : " (EN ESPERA de recursos)"));
        log.info("Tutoria consolidada: materia={} inscritos={} estado={}",
                materia.getCodigo(), pendientes.size(), sesion.getEstado());
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
        auditoriaService.registrar(AuditoriaService.CONSOLIDA_TUTORIA, "Tutoria", String.valueOf(sesionId),
                AuditoriaService.OK, "Asignacion manual de docente y aula");
        return sesionResponse(saved);
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

    @Transactional(readOnly = true)
    public List<SesionTutoriaResponse> listarSesiones() {
        Usuario yo = currentUser.obtenerActual();
        List<SesionTutoria> sesiones;
        if (yo.getTipoUsuario() == TipoUsuario.ADMINISTRATIVO) {
            sesiones = sesionRepository.findAllByOrderByFechaCreacionDesc();
        } else if (yo.getTipoUsuario() == TipoUsuario.DOCENTE) {
            sesiones = sesionRepository.findByDocente_IdOrderByFechaHoraInicioAsc(yo.getId());
        } else {
            sesiones = demandaRepository.findByAlumno_IdOrderByFechaInscripcionDesc(yo.getId()).stream()
                    .map(DemandaTutoria::getSesion).filter(Objects::nonNull).distinct().toList();
        }
        return sesiones.stream().map(this::sesionResponse).toList();
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

    private SesionTutoriaResponse sesionResponse(SesionTutoria s) {
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
                .build();
    }
}
