package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.TipoBloqueo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloqueHorarioResponse {

    private Long id;
    private TipoBloqueo tipoBloqueo;
    private Long espacioFisicoId;
    private Long recursoId;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String motivo;
    private Boolean activo;
}