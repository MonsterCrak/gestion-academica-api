package edu.upc.sistema.gestionacademicaapi.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Creación directa de una sesión de tutoría por un docente o administrador,
 * con cupo de participantes e inscripción libre de alumnos hasta llenarlo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearSesionTutoriaRequest {

    @NotNull
    private Long materiaId;

    /** Docente a cargo. Obligatorio si lo crea un administrador; opcional si lo crea el propio docente. */
    private Long docenteId;

    /** Aula donde se dictará (opcional). */
    private Long aulaId;

    @NotNull
    @Future
    private LocalDateTime fechaHoraInicio;

    @NotNull
    @Min(1)
    @Max(500)
    private Integer cupo;
}
