package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.EstadoConfirmacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmacionAlumnoResponse {

    private Long id;
    private Long solicitudId;
    private Long alumnoId;
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