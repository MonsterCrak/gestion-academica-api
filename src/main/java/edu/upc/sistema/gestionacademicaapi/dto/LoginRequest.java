package edu.upc.sistema.gestionacademicaapi.dto;

import jakarta.validation.constraints.NotBlank;
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
public class LoginRequest {

    @NotBlank
    @Size(max = 80)
    private String identificadorCorporativo;

    @NotBlank
    @Size(min = 8, max = 128)
    private String password;
}