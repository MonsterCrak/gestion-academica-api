package edu.upc.sistema.gestionacademicaapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Indicadores operativos para el panel de administración (HU-21). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteDashboardResponse {

    private long totalEquipos;
    private long equiposDisponibles;
    private long equiposPrestados;
    private double ocupacionEquiposPct;

    private long prestamosActivos;
    private long prestamosVencidos;
    private double devolucionesATiempoPct;

    private long totalUsuarios;
    private long usuariosConDeuda;
    private long penalizacionesActivas;
    private double tasaMorosidadPct;

    private long aulasActivas;
    private long reservasAprobadas;

    private long sesionesTutoriaConfirmadas;
    private long demandaTutoriasPendiente;
}
