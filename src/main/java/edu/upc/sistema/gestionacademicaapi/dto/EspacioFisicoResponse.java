package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio;
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
public class EspacioFisicoResponse {

    private Long id;
    private String codigo;
    private TipoEspacio tipoEspacio;
    private Integer aforo;
    private Boolean permitirPrestamoIndividual;
    private Boolean permitirReservaCompleta;
    private Boolean activo;
}
