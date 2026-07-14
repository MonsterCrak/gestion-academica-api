package edu.upc.sistema.gestionacademicaapi.dto;

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
public class DeudaUpdateRequest {

    @NotNull
    private Boolean tieneDeuda;

    @Size(max = 300)
    private String motivo;
}
