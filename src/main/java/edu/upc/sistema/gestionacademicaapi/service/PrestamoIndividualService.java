package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.PrestamoCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.PrestamoResponse;
import edu.upc.sistema.gestionacademicaapi.entity.BloqueHorario;
import edu.upc.sistema.gestionacademicaapi.entity.CategoriaPolitica;
import edu.upc.sistema.gestionacademicaapi.entity.PrestamoIndividual;
import edu.upc.sistema.gestionacademicaapi.entity.Recurso;
import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoPrestamo;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoRecurso;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrestamoIndividualService {

    private final PrestamoIndividualRepository repository;
    private final RecursoService recursoService;
    private final RecursoRepository recursoRepository;
    private final BloqueHorarioRepository bloqueHorarioRepository;
    private final CurrentUserService currentUser;

    @Transactional
    public PrestamoResponse solicitar(PrestamoCreateRequest req) {
        Usuario yo = currentUser.obtenerActual();
        if (yo.getTipoUsuario() != TipoUsuario.ESTUDIANTE) {
            throw new ReglaNegocioException("TIPO_INVALIDO", "Solo estudiantes pueden solicitar prestamos");
        }
        if (req.getAceptoTerminos() == null || !req.getAceptoTerminos()) {
            throw new ReglaNegocioException("RN-01", "Debe aceptar los terminos y responsabilidades para tomar el prestamo");
        }
        if (!req.getFechaInicio().isBefore(req.getFechaFin())) {
            throw new ReglaNegocioException("FECHA_INVALIDA", "fechaInicio debe ser anterior a fechaFin");
        }

        Recurso recurso = recursoService.buscarPorId(req.getRecursoId());
        CategoriaPolitica categoria = recurso.getCategoria();
        if (categoria == null) {
            throw new ReglaNegocioException("SIN_CATEGORIA", "El recurso no tiene categoria asignada");
        }

        long prestamosActivos = repository.countByUsuarioSolicitante_IdAndEstado(yo.getId(), EstadoPrestamo.ACTIVO);
        if (prestamosActivos >= categoria.getMaxItemsPorAlumno()) {
            throw new ReglaNegocioException("RN-02",
                    "Limite de items simultaneos alcanzado para la categoria " + categoria.getNombreCategoria()
                            + " (max=" + categoria.getMaxItemsPorAlumno() + ")");
        }

        if (recurso.getEstado() == EstadoRecurso.MANTENIMIENTO) {
            throw new ReglaNegocioException("REPARACION", "El recurso esta en mantenimiento");
        }
        if (!recurso.getEstado().equals(EstadoRecurso.DISPONIBLE)) {
            throw new ReglaNegocioException("REPETIDO", "El recurso ya tiene un prestamo activo");
        }

        if (Boolean.TRUE.equals(recurso.getRequiereUbicacionFisica())
                || recurso.getTipoMovilidad() == TipoMovilidad.FIJO_EN_AULA) {
            validarHorarioInstitucional(recurso);
        }

        recurso.setEstado(EstadoRecurso.PRESTADO);
        recursoRepository.save(recurso);

        PrestamoIndividual pi = PrestamoIndividual.builder()
                .recurso(recurso)
                .usuarioSolicitante(yo)
                .fechaInicio(req.getFechaInicio())
                .fechaFin(req.getFechaFin())
                .aceptoTerminos(Boolean.TRUE)
                .estado(EstadoPrestamo.ACTIVO)
                .build();

        return toResponse(repository.save(pi));
    }

    @Transactional
    public PrestamoResponse devolver(Long prestamoId) {
        Usuario yo = currentUser.obtenerActual();
        PrestamoIndividual pi = repository.findById(prestamoId)
                .orElseThrow(() -> new ReglaNegocioException("NO_ENCONTRADO", "Prestamo no encontrado"));

        if (!pi.getUsuarioSolicitante().getId().equals(yo.getId())
                && yo.getTipoUsuario() != TipoUsuario.ADMINISTRATIVO) {
            throw new ReglaNegocioException("ACCESO_DENEGADO", "No es dueno del prestamo");
        }
        if (pi.getEstado() != EstadoPrestamo.ACTIVO) {
            throw new ReglaNegocioException("ESTADO_INVALIDO", "El prestamo no esta activo");
        }
        pi.setEstado(EstadoPrestamo.DEVUELTO);

        Recurso r = pi.getRecurso();
        r.setEstado(EstadoRecurso.DISPONIBLE);
        recursoRepository.save(r);

        return toResponse(repository.save(pi));
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> listarMios() {
        Usuario yo = currentUser.obtenerActual();
        return repository.findByUsuarioSolicitante_IdAndEstado(yo.getId(), EstadoPrestamo.ACTIVO)
                .stream().map(this::toResponse).toList();
    }

    private void validarHorarioInstitucional(Recurso recurso) {
        List<BloqueHorario> bloques = bloqueHorarioRepository
                .findByRecurso_IdAndTipoBloqueoAndActivoTrue(recurso.getId(), TipoBloqueo.FUERA_ATENCION_EQUIPOS);

        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fin = inicio.plusHours(1);

        for (BloqueHorario b : bloques) {
            if (solapaFranja(b, inicio, fin)) {
                throw new ReglaNegocioException("RN-08",
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

    private PrestamoResponse toResponse(PrestamoIndividual p) {
        return PrestamoResponse.builder()
                .id(p.getId())
                .recursoId(p.getRecurso().getId())
                .recursoCodigo(p.getRecurso().getCodigoInventario())
                .recursoNombre(p.getRecurso().getNombre())
                .usuarioSolicitanteId(p.getUsuarioSolicitante().getId())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .aceptoTerminos(p.getAceptoTerminos())
                .estado(p.getEstado())
                .build();
    }
}
