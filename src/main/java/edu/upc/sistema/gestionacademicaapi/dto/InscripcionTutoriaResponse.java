package edu.upc.sistema.gestionacademicaapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Estado de la inscripción de un alumno en una demanda/sesión de tutoría (HU-16/18). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscripcionTutoriaResponse {

    private Long id;
    private String materiaCodigo;
    private String materiaNombre;
    private LocalDateTime fechaInscripcion;
    private Long sesionId;
    /** PENDIENTE_QUORUM mientras no se consolida; luego el estado de la sesión. */
    private String estado;
    private Boolean enListaEspera;
    private LocalDateTime fechaHoraInicio;
}
