package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.ModalidadPrestamo;
import jakarta.validation.constraints.AssertTrue;
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
public class PrestamoCreateRequest {

    @NotNull
    private Long recursoId;

    @NotNull
    private ModalidadPrestamo modalidad;

    /** HU-11: la aceptación del contrato de responsabilidad es obligatoria. */
    @AssertTrue(message = "debe aceptar los terminos y responsabilidades (HU-11)")
    private Boolean aceptoTerminos;
}
