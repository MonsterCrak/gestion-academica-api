package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.ModoResolucion;
import edu.upc.sistema.gestionacademicaapi.enums.OrigenPenalizacion;
import edu.upc.sistema.gestionacademicaapi.enums.TipoPenalizacion;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
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
public class PenalizacionCreateRequest {

    @NotNull
    private Long usuarioId;

    @NotNull
    private TipoPenalizacion tipo;

    /** Módulo asociado (préstamo, reserva, tutoría o general). Por defecto GENERAL. */
    private OrigenPenalizacion origen;

    @NotBlank
    @Size(max = 300)
    private String motivo;

    @NotNull
    @Min(1)
    private Integer diasBloqueo;

    /** Cómo resolver la penalización. Por defecto SUSPENSION_RECURSOS. */
    private ModoResolucion modoResolucion;

    /** Monto a pagar/descontar (solo si el modo es económico). */
    @DecimalMin("0.0")
    private BigDecimal monto;

    /** Instrucciones para que el usuario resuelva la penalización. */
    @Size(max = 500)
    private String instruccionesResolucion;
}
