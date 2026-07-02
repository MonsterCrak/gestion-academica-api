package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.TipoBloqueo;
import jakarta.validation.constraints.NotNull;
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
public class BloqueHorarioCreateRequest {

    @NotNull
    private TipoBloqueo tipoBloqueo;

    private UUID espacioFisicoId;

    private UUID recursoId;

    @NotNull
    private LocalDate fechaDesde;

    private LocalDate fechaHasta;

    @NotNull
    private LocalTime horaInicio;

    @NotNull
    private LocalTime horaFin;

    private String motivo;
}
