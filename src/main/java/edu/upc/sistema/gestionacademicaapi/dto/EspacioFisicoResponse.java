package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio;
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
public class EspacioFisicoResponse {

    private UUID id;
    private String codigo;
    private TipoEspacio tipoEspacio;
    private Integer aforo;
    private Boolean permitirPrestamoIndividual;
    private Boolean permitirReservaCompleta;
}
