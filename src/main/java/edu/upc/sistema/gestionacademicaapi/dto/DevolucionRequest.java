package edu.upc.sistema.gestionacademicaapi.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevolucionRequest {

    /** Estado del equipo al devolver: BUENO o DANADO. Si es DANADO el equipo pasa a mantenimiento. */
    private String estadoEquipo;

    @Size(max = 300)
    private String observaciones;
}
