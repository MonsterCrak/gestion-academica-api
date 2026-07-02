package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.TipoBloqueo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloqueHorarioResponse {

    private UUID id;
    private TipoBloqueo tipoBloqueo;
    private UUID espacioFisicoId;
    private UUID recursoId;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String motivo;
    private Boolean activo;
}
