package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
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
public class UsuarioCreateRequest {

    @NotNull
    private TipoUsuario tipoUsuario;

    @NotBlank
    @Size(max = 80)
    private String identificadorCorporativo;

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @NotBlank
    @Size(max = 160)
    private String apellidos;

    private Long carreraId;

    @NotBlank
    @Size(min = 8, max = 128)
    private String password;
}