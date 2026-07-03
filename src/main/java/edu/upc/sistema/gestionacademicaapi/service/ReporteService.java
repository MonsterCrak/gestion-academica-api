package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.ReporteCargaMensualResponse;
import edu.upc.sistema.gestionacademicaapi.entity.Materia;
import edu.upc.sistema.gestionacademicaapi.entity.RegistroHorasTutoria;
import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.AccesoNoAutorizadoException;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.repository.RegistroHorasTutoriaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final RegistroHorasTutoriaRepository registroRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public ReporteCargaMensualResponse cargaMensual(Long docenteId, int anio, int mes) {
        Usuario docente = usuarioRepository.findById(docenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", docenteId));

        LocalDateTime desde = LocalDate.of(anio, mes, 1).atStartOfDay();
        LocalDateTime hasta = desde.plusMonths(1);

        List<RegistroHorasTutoria> registros = registroRepository
                .findByDocente_IdAndFechaHoraInicioRealBetween(docente.getId(), desde, hasta);

        List<ReporteCargaMensualResponse.DetalleCarga> detalle = registros.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getAnulado()))
                .map(this::toDetalle).toList();

        BigDecimal total = detalle.stream()
                .map(d -> d.getHorasRectificadas() != null ? d.getHorasRectificadas() : d.getHorasEfectivas())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ReporteCargaMensualResponse.builder()
                .docenteId(docente.getId())
                .identificadorDocente(docente.getIdentificadorCorporativo())
                .nombreDocente(docente.getNombre() + " " + docente.getApellidos())
                .anio(anio)
                .mes(mes)
                .totalHoras(total)
                .detalle(detalle)
                .build();
    }

    @Transactional(readOnly = true)
    public ReporteCargaMensualResponse miCargaMensual(int anio, int mes) {
        throw new UnsupportedOperationException(
                "Use cargaMensual con el id del docente autenticado desde el controller");
    }

    public Long validarDocenteActualYCargar(CurrentUserService currentUser, int anio, int mes) {
        Usuario yo = currentUser.obtenerActual();
        if (yo.getTipoUsuario() != TipoUsuario.DOCENTE) {
            throw new AccesoNoAutorizadoException("Esta vista es solo para docentes");
        }
        cargaMensual(yo.getId(), anio, mes);
        return yo.getId();
    }

    private ReporteCargaMensualResponse.DetalleCarga toDetalle(RegistroHorasTutoria r) {
        Materia m = r.getMateria();
        return ReporteCargaMensualResponse.DetalleCarga.builder()
                .solicitudId(r.getSolicitud().getId())
                .materiaId(m.getId())
                .materiaCodigo(m.getCodigo())
                .materiaNombre(m.getNombre())
                .fechaHoraInicio(r.getFechaHoraInicioReal())
                .fechaHoraFin(r.getFechaHoraFinReal())
                .horasEfectivas(r.getHorasEfectivas())
                .horasRectificadas(r.getHorasRectificadas())
                .build();
    }
}
