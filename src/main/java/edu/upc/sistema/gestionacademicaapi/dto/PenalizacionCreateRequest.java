package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.TipoPenalizacion;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank
    @Size(max = 300)
    private String motivo;

    @NotNull
    @Min(1)
    private Integer diasBloqueo;
}
