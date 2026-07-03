package edu.upc.sistema.gestionacademicaapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolverRevisionRequest {

    @NotNull
    private Decision decision;

    private BigDecimal horasRectificadas;

    @NotBlank
    private String motivo;

    public enum Decision {
        RATIFICAR,
        REVOCAR,
        RECTIFICAR
    }
}
