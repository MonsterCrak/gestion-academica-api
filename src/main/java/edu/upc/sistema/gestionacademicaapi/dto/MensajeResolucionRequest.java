package edu.upc.sistema.gestionacademicaapi.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Mensaje opcional del administrador al enviar las instrucciones de resolución. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeResolucionRequest {

    @Size(max = 500)
    private String mensaje;
}
