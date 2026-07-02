package edu.upc.sistema.gestionacademicaapi.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestamoCreateRequest {

    @NotNull
    private Long recursoId;

    @NotNull
    private LocalDateTime fechaInicio;

    @NotNull
    private LocalDateTime fechaFin;

    @AssertTrue(message = "debe aceptar los terminos y responsabilidades (RN-01)")
    private Boolean aceptoTerminos;
}