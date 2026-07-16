package edu.upc.sistema.gestionacademicaapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Alumno inscrito en una sesión de tutoría (para el docente/admin dueño). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscritoTutoriaResponse {

    private Long alumnoId;
    private String identificadorCorporativo;
    private String nombre;
    private String apellidos;
    private String email;
    private LocalDateTime fechaInscripcion;
    private boolean enListaEspera;
}
