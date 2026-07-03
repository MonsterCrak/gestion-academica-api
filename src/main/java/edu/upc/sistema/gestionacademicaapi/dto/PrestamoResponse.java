package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.EstadoPrestamo;
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
public class PrestamoResponse {

    private Long id;
    private Long recursoId;
    private String recursoCodigo;
    private String recursoNombre;
    private Long usuarioSolicitanteId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Boolean aceptoTerminos;
    private EstadoPrestamo estado;
}