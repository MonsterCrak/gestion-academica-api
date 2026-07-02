package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.TipoMovilidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecursoCreateRequest {

    @NotNull
    private UUID categoriaId;

    private String numeroSerie;

    @NotBlank
    private String codigoInventario;

    @NotBlank
    private String nombre;

    @NotNull
    private TipoMovilidad tipoMovilidad;

    private UUID espacioActualId;
}
