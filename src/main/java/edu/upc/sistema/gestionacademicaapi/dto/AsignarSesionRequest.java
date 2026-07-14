package edu.upc.sistema.gestionacademicaapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Asignación manual de docente y aula a una sesión en espera de recursos (HU-18/CP-15). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarSesionRequest {

    @NotNull
    private Long docenteId;

    @NotNull
    private Long aulaId;
}
