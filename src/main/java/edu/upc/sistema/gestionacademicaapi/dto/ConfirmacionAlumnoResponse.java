package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.EstadoConfirmacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmacionAlumnoResponse {

    private UUID id;
    private UUID solicitudId;
    private UUID alumnoId;
    private String identificadorAlumno;
    private String nombreAlumno;
    private LocalDateTime fechaConfirmacion;
    private EstadoConfirmacion estadoConfirmacion;
    private Boolean asistio;
    private LocalDateTime fechaRegistroAsistencia;
    private Boolean apelo;
    private String motivoApelacion;
    private LocalDateTime fechaApelacion;
}
