package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio;
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
public class EspacioFisicoUpdateRequest {

    private TipoEspacio tipoEspacio;

    @Min(value = 1, message = "el aforo debe ser >= 1")
    private Integer aforo;

    private Boolean permitirPrestamoIndividual;

    private Boolean permitirReservaCompleta;
}
