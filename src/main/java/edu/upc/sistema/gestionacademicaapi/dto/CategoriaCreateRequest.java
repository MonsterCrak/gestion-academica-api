package edu.upc.sistema.gestionacademicaapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CategoriaCreateRequest {

    @NotBlank
    private String nombreCategoria;

    @NotNull
    @Min(value = 1)
    private Integer maxItemsPorAlumno;

    @NotNull
    @Min(value = 1)
    private Integer tiempoMaximoHoras;
}
