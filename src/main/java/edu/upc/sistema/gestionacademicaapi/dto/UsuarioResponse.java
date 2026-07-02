package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {

    private UUID id;
    private TipoUsuario tipoUsuario;
    private String identificadorCorporativo;
    private String nombre;
    private String apellidos;
    private UUID carreraId;
    private Boolean activo;
}
