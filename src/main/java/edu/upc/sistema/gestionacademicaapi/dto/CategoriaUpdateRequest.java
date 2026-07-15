package edu.upc.sistema.gestionacademicaapi.dto;

import jakarta.validation.constraints.Min;
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
public class CategoriaUpdateRequest {

    private String nombreCategoria;

    @Min(value = 1)
    private Integer maxItemsPorAlumno;

    @Min(value = 1)
    private Integer tiempoMaximoHoras;

    private Boolean permiteExtension;

    @Min(value = 1)
    private Integer horasExtension;
}
