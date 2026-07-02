package edu.upc.sistema.gestionacademicaapi.job;

import edu.upc.sistema.gestionacademicaapi.entity.SolicitudTutoria;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoSolicitud;
import edu.upc.sistema.gestionacademicaapi.repository.ConfirmacionTutoriaAlumnoRepository;
import edu.upc.sistema.gestionacademicaapi.repository.SolicitudTutoriaRepository;
import edu.upc.sistema.gestionacademicaapi.service.SolicitudTutoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CierreTutoriaJob {

    private static final int UMBRAL_APELACIONES_ABS = 3;
    private static final double UMBRAL_APELACIONES_PORC = 0.65;
    private static final int PESO_VOTO_DOCENTE = 3;

    private final SolicitudTutoriaRepository solicitudRepository;
    private final ConfirmacionTutoriaAlumnoRepository confirmacionRepository;
    private final SolicitudTutoriaService solicitudService;

    @Scheduled(fixedDelayString = "${app.jobs.cierre-confirmacion.fixed-delay-ms:60000}",
            initialDelay = 30_000)
    @Transactional
    public void cerrarPorFaltaDeConfirmacionDocente() {
        try {
            LocalDateTime limite = LocalDateTime.now().minus(Duration.ofHours(24));
            List<SolicitudTutoria> candidatas = solicitudRepository
                    .findByEstadoAndFechaHoraFinBefore(EstadoSolicitud.CONFIRMADA, limite);

            int cerradas = 0;
            for (SolicitudTutoria s : candidatas) {
                if (!Boolean.TRUE.equals(s.getDocenteConfirmoRealizacion())) {
                    s.setEstado(EstadoSolicitud.NO_REALIZADA);
                    solicitudRepository.save(s);
                    cerradas++;
                }
            }
            if (cerradas > 0) {
                log.info("Job A: {} solicitudes cerradas como NO_REALIZADA por falta de confirmacion docente", cerradas);
            }
        } catch (Exception ex) {
            // Resiliencia: si las tablas aun no estan listas (BD recien creada) o hay
            // cualquier transitorio, este job se re-ejecutara en el siguiente fixedDelay.
            log.warn("Job A omitido este ciclo ({}) — se reintentara en el proximo tick.", ex.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${app.jobs.evaluacion-apelaciones.fixed-delay-ms:600000}",
            initialDelay = 60_000)
    @Transactional
    public void evaluarApelaciones() {
        try {
            LocalDateTime limite = LocalDateTime.now().minus(Duration.ofDays(7));
            List<SolicitudTutoria> candidatas = solicitudRepository
                    .findByEstadoAndFechaHoraFinBefore(EstadoSolicitud.REALIZADA, limite);

            int promovidas = 0;
            for (SolicitudTutoria s : candidatas) {
                long apelantes = confirmacionRepository.countBySolicitud_IdAndApeloTrue(s.getId());
                int total = s.getTotalConfirmados() != null ? s.getTotalConfirmados() : 0;

                long votosNoSeRealizo = apelantes;
                long votosSeRealizo = total - apelantes + PESO_VOTO_DOCENTE;
                boolean mayoriaNoSeRealizo = votosNoSeRealizo > votosSeRealizo;

                boolean superaAbsoluto = apelantes > UMBRAL_APELACIONES_ABS;
                boolean superaPorcentaje = total > 0 && ((double) apelantes / total) >= UMBRAL_APELACIONES_PORC;

                if (superaAbsoluto || superaPorcentaje || mayoriaNoSeRealizo) {
                    s.setEstado(EstadoSolicitud.REALIZADA_EN_REVISION);
                    solicitudRepository.save(s);
                    promovidas++;
                    log.info("Job B: solicitud {} promovida a REALIZADA_EN_REVISION (apelantes={}, total={})",
                            s.getId(), apelantes, total);
                }
            }
            if (promovidas > 0) {
                log.info("Job B: {} solicitudes promovidas a REALIZADA_EN_REVISION", promovidas);
            }
        } catch (Exception ex) {
            log.warn("Job B omitido este ciclo ({}) — se reintentara en el proximo tick.", ex.getMessage());
        }
    }
}
