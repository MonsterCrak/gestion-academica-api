package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio;
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
public class EspacioFisicoCreateRequest {

    @NotBlank
    private String codigo;

    @NotNull
    private TipoEspacio tipoEspacio;

    @NotNull
    @Min(value = 1, message = "el aforo debe ser >= 1")
    private Integer aforo;

    @NotNull
    private Boolean permitirPrestamoIndividual;

    @NotNull
    private Boolean permitirReservaCompleta;
}
