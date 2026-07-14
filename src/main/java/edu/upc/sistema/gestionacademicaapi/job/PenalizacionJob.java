package edu.upc.sistema.gestionacademicaapi.job;

import edu.upc.sistema.gestionacademicaapi.service.PenalizacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * HU-20: levanta automaticamente las penalizaciones vencidas y rehabilita a los usuarios.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PenalizacionJob {

    private final PenalizacionService penalizacionService;

    @Scheduled(fixedDelayString = "${app.jobs.penalizaciones.fixed-delay-ms:300000}", initialDelay = 45_000)
    public void levantarPenalizacionesVencidas() {
        try {
            int levantadas = penalizacionService.levantarVencidas();
            if (levantadas > 0) {
                log.info("PenalizacionJob: {} penalizaciones vencidas levantadas", levantadas);
            }
        } catch (Exception ex) {
            log.warn("PenalizacionJob omitido este ciclo ({}) — se reintentara en el proximo tick.", ex.getMessage());
        }
    }
}
