package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.TipoMovilidad;
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
public class RecursoUpdateRequest {

    private Long categoriaId;

    @Size(max = 80)
    private String numeroSerie;

    @Size(max = 160)
    private String nombre;

    private TipoMovilidad tipoMovilidad;

    private Long espacioActualId;
}
