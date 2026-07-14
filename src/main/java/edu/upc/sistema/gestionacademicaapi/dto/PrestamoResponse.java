package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.EstadoPrestamo;
import edu.upc.sistema.gestionacademicaapi.enums.ModalidadPrestamo;
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
    private String usuarioNombre;
    private ModalidadPrestamo modalidad;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime fechaDevolucion;
    private Boolean aceptoTerminos;
    private EstadoPrestamo estado;
    /** Días de retraso respecto a fechaFin (0 si no hay retraso). */
    private long diasRetraso;
}
