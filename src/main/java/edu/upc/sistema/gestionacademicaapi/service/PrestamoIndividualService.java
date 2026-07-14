package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.DevolucionRequest;
import edu.upc.sistema.gestionacademicaapi.dto.PrestamoCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.PrestamoResponse;
import edu.upc.sistema.gestionacademicaapi.entity.BloqueHorario;
import edu.upc.sistema.gestionacademicaapi.entity.CategoriaPolitica;
import edu.upc.sistema.gestionacademicaapi.entity.PrestamoIndividual;
import edu.upc.sistema.gestionacademicaapi.entity.Recurso;
import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoPrestamo;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoRecurso;
import edu.upc.sistema.gestionacademicaapi.enums.ModalidadPrestamo;
import edu.upc.sistema.gestionacademicaapi.enums.TipoBloqueo;
import edu.upc.sistema.gestionacademicaapi.enums.TipoMovilidad;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.ReglaNegocioException;
import edu.upc.sistema.gestionacademicaapi.repository.BloqueHorarioRepository;
import edu.upc.sistema.gestionacademicaapi.repository.PrestamoIndividualRepository;
import edu.upc.sistema.gestionacademicaapi.repository.RecursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Préstamo de equipos (HU-09/10/11/12). Aplica bloqueo por deuda/penalización (RN-01/HU-19),
 * restricción horaria en modalidad campus (HU-09) y penalización automática por retraso (HU-12/HU-20).
 */
@Service
@RequiredArgsConstructor
public class PrestamoIndividualService {

    private static final int DIAS_CICLO_SOCIOECONOMICO = 120;
    private static final String VERSION_TERMINOS = "contrato-responsabilidad-v1.0";

    private final PrestamoIndividualRepository repository;
    private final RecursoService recursoService;
    private final RecursoRepository recursoRepository;
    private final BloqueHorarioRepository bloqueHorarioRepository;
    private final CurrentUserService currentUser;
    private final PenalizacionService penalizacionService;
    private final AuditoriaService auditoriaService;

    @Transactional
    public PrestamoResponse solicitar(PrestamoCreateRequest req) {
        Usuario yo = currentUser.obtenerActual();
        if (yo.getTipoUsuario() != TipoUsuario.ESTUDIANTE) {
            throw new ReglaNegocioException("TIPO_INVALIDO", "Solo estudiantes pueden solicitar prestamos");
        }
        // RN-01 / HU-19: bloqueo por deuda o penalización vigente.
        penalizacionService.verificarPuedeOperar(yo, "Solicitud de prestamo");

        if (!Boolean.TRUE.equals(req.getAceptoTerminos())) {
            throw new ReglaNegocioException("RN-04",
                    "Debe aceptar el contrato de responsabilidad para retirar el equipo");
        }

        Recurso recurso = recursoService.buscarPorId(req.getRecursoId());
        CategoriaPolitica categoria = recurso.getCategoria();
        if (categoria == null) {
            throw new ReglaNegocioException("SIN_CATEGORIA", "El recurso no tiene categoria asignada");
        }

        long activos = repository.countByUsuarioSolicitante_IdAndEstado(yo.getId(), EstadoPrestamo.ACTIVO);
        if (activos >= categoria.getMaxItemsPorAlumno()) {
            throw new ReglaNegocioException("LIMITE_ITEMS",
                    "Limite de items simultaneos alcanzado para la categoria " + categoria.getNombreCategoria()
                            + " (max=" + categoria.getMaxItemsPorAlumno() + ")");
        }

        if (recurso.getEstado() == EstadoRecurso.MANTENIMIENTO) {
            throw new ReglaNegocioException("MANTENIMIENTO", "El recurso esta en mantenimiento");
        }
        if (recurso.getEstado() == EstadoRecurso.DADO_DE_BAJA) {
            throw new ReglaNegocioException("BAJA", "El recurso fue dado de baja");
        }
        if (recurso.getEstado() != EstadoRecurso.DISPONIBLE) {
            throw new ReglaNegocioException("NO_DISPONIBLE", "El recurso no esta disponible");
        }

        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fin;
        if (req.getModalidad() == ModalidadPrestamo.CAMPUS) {
            if (Boolean.TRUE.equals(recurso.getRequiereUbicacionFisica())
                    || recurso.getTipoMovilidad() == TipoMovilidad.FIJO_EN_AULA) {
                validarHorarioInstitucional(recurso);
            }
            int horas = categoria.getTiempoMaximoHoras() != null ? categoria.getTiempoMaximoHoras() : 4;
            fin = inicio.plusHours(horas);
        } else {
            if (recurso.getTipoMovilidad() == TipoMovilidad.FIJO_EN_AULA) {
                throw new ReglaNegocioException("NO_TRANSPORTABLE",
                        "Un recurso fijo en aula no puede prestarse en modalidad socioeconomica");
            }
            fin = inicio.plusDays(DIAS_CICLO_SOCIOECONOMICO);
        }

        recurso.setEstado(EstadoRecurso.PRESTADO);
        recursoRepository.save(recurso);

        PrestamoIndividual pi = PrestamoIndividual.builder()
                .recurso(recurso)
                .usuarioSolicitante(yo)
                .modalidad(req.getModalidad())
                .fechaInicio(inicio)
                .fechaFin(fin)
                .aceptoTerminos(Boolean.TRUE)
                .versionTerminos(VERSION_TERMINOS)
                .fechaAceptacionTerminos(inicio)
                .estado(EstadoPrestamo.ACTIVO)
                .build();

        PrestamoIndividual saved = repository.save(pi);
        auditoriaService.registrar(AuditoriaService.SOLICITA_PRESTAMO, "Prestamo",
                String.valueOf(saved.getId()), AuditoriaService.OK,
                "Modalidad " + req.getModalidad() + " sobre " + recurso.getCodigoInventario());
        return toResponse(saved);
    }

    @Transactional
    public PrestamoResponse devolver(Long prestamoId, DevolucionRequest req) {
        Usuario yo = currentUser.obtenerActual();
        PrestamoIndividual pi = repository.findById(prestamoId)
                .orElseThrow(() -> new ReglaNegocioException("NO_ENCONTRADO", "Prestamo no encontrado"));

        boolean esAdmin = yo.getTipoUsuario() == TipoUsuario.ADMINISTRATIVO;
        if (!esAdmin && !pi.getUsuarioSolicitante().getId().equals(yo.getId())) {
            throw new ReglaNegocioException("ACCESO_DENEGADO", "No es dueno del prestamo");
        }
        if (pi.getEstado() != EstadoPrestamo.ACTIVO) {
            throw new ReglaNegocioException("ESTADO_INVALIDO", "El prestamo no esta activo");
        }

        LocalDateTime ahora = LocalDateTime.now();
        String estadoEquipo = (req != null && req.getEstadoEquipo() != null && !req.getEstadoEquipo().isBlank())
                ? req.getEstadoEquipo() : "BUENO";
        pi.setFechaDevolucion(ahora);
        pi.setEstado(EstadoPrestamo.DEVUELTO);
        pi.setEstadoEquipoDevolucion(estadoEquipo);
        pi.setObservacionesDevolucion(req != null ? req.getObservaciones() : null);

        boolean danado = estadoEquipo.equalsIgnoreCase("DANADO") || estadoEquipo.equalsIgnoreCase("DAÑADO");
        Recurso r = pi.getRecurso();
        r.setEstado(danado ? EstadoRecurso.MANTENIMIENTO : EstadoRecurso.DISPONIBLE);
        recursoRepository.save(r);

        long diasRetraso = 0;
        if (ahora.isAfter(pi.getFechaFin())) {
            diasRetraso = Math.max(1, Duration.between(pi.getFechaFin(), ahora).toDays());
            // HU-12 / HU-20: penalización automática por devolución fuera de plazo.
            penalizacionService.aplicarPorRetraso(pi.getUsuarioSolicitante(), pi.getId(), diasRetraso);
        }

        PrestamoIndividual saved = repository.save(pi);
        auditoriaService.registrar(AuditoriaService.DEVUELVE_PRESTAMO, "Prestamo",
                String.valueOf(saved.getId()), AuditoriaService.OK,
                "Devolucion" + (diasRetraso > 0 ? " con " + diasRetraso + " dia(s) de retraso" : " a tiempo")
                        + (danado ? " — equipo danado" : ""));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> listarMios() {
        Usuario yo = currentUser.obtenerActual();
        return repository.findByUsuarioSolicitante_IdOrderByFechaInicioDesc(yo.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> listarActivos() {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        return repository.findByEstadoOrderByFechaFinAsc(EstadoPrestamo.ACTIVO)
                .stream().map(this::toResponse).toList();
    }

    private void validarHorarioInstitucional(Recurso recurso) {
        List<BloqueHorario> bloques = bloqueHorarioRepository
                .findByRecurso_IdAndTipoBloqueoAndActivoTrue(recurso.getId(), TipoBloqueo.FUERA_ATENCION_EQUIPOS);

        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fin = inicio.plusHours(1);

        for (BloqueHorario b : bloques) {
            if (solapaFranja(b, inicio, fin)) {
                throw new ReglaNegocioException("RN-02",
                        "El recurso esta fuera de horario institucional: " + b.getMotivo());
            }
        }
    }

    private boolean solapaFranja(BloqueHorario b, LocalDateTime inicio, LocalDateTime fin) {
        LocalDate fecha = inicio.toLocalDate();
        if (fecha.isBefore(b.getFechaDesde())) return false;
        if (b.getFechaHasta() != null && fecha.isAfter(b.getFechaHasta())) return false;
        LocalTime hInicio = inicio.toLocalTime();
        LocalTime hFin = fin.toLocalTime();
        return hFin.compareTo(b.getHoraInicio()) > 0 && hInicio.compareTo(b.getHoraFin()) < 0;
    }

    private long calcularRetraso(PrestamoIndividual p) {
        LocalDateTime referencia = p.getFechaDevolucion() != null ? p.getFechaDevolucion() : LocalDateTime.now();
        if (p.getFechaFin() != null && referencia.isAfter(p.getFechaFin())) {
            return Math.max(0, Duration.between(p.getFechaFin(), referencia).toDays());
        }
        return 0;
    }

    private PrestamoResponse toResponse(PrestamoIndividual p) {
        return PrestamoResponse.builder()
                .id(p.getId())
                .recursoId(p.getRecurso().getId())
                .recursoCodigo(p.getRecurso().getCodigoInventario())
                .recursoNombre(p.getRecurso().getNombre())
                .usuarioSolicitanteId(p.getUsuarioSolicitante().getId())
                .usuarioNombre(p.getUsuarioSolicitante().getNombre() + " " + p.getUsuarioSolicitante().getApellidos())
                .modalidad(p.getModalidad())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .fechaDevolucion(p.getFechaDevolucion())
                .aceptoTerminos(p.getAceptoTerminos())
                .estado(p.getEstado())
                .diasRetraso(calcularRetraso(p))
                .build();
    }
}
