package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.EstadoSesionTutoria;
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
public class SesionTutoriaResponse {

    private Long id;
    private String materiaCodigo;
    private String materiaNombre;
    private String docenteNombre;
    private String aulaCodigo;
    private Integer cupo;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private EstadoSesionTutoria estado;
    private long inscritos;
    private long enListaEspera;

    /** True si admite inscripción libre de alumnos hasta el cupo (sesión creada por docente/admin). */
    private boolean abierta;

    /** True si el alumno autenticado ya está inscrito en esta sesión. */
    private boolean yaInscrito;
}
