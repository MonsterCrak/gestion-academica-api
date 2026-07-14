package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.EstadoUsuario;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
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
public class UsuarioResponse {

    private Long id;
    private TipoUsuario tipoUsuario;
    private String identificadorCorporativo;
    private String email;
    private String nombre;
    private String apellidos;
    private Long carreraId;
    private Boolean activo;
    private EstadoUsuario estado;
    private Boolean tieneDeuda;
    private LocalDateTime penalizadoHasta;
    private Boolean bloqueadoParaOperar;
}
