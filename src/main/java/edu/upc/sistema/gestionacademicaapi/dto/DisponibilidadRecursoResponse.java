package edu.upc.sistema.gestionacademicaapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Resumen de disponibilidad en tiempo real del inventario (HU-07). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadRecursoResponse {

    private long total;
    private long disponibles;
    private long prestados;
    private long reservados;
    private long mantenimiento;
    private long dadosDeBaja;
}
