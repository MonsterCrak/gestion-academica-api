package edu.upc.sistema.gestionacademicaapi.dto;

import jakarta.validation.constraints.NotBlank;
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
public class MateriaCreateRequest {

    @NotBlank
    private String codigo;

    @NotBlank
    private String nombre;

    private String departamento;
}
