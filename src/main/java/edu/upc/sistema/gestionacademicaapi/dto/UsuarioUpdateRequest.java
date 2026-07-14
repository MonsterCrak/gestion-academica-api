package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
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
public class UsuarioUpdateRequest {

    private TipoUsuario tipoUsuario;

    @Size(max = 80)
    private String identificadorCorporativo;

    @Size(max = 120)
    private String nombre;

    @Size(max = 160)
    private String apellidos;

    private Long carreraId;

    @Size(min = 8, max = 128)
    private String password;
}