package edu.upc.sistema.gestionacademicaapi.dto;

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
public class AuditoriaResponse {

    private Long id;
    private Long usuarioId;
    private String usuarioIdentificador;
    private String accion;
    private String entidad;
    private String entidadId;
    private String resultado;
    private String ip;
    private String userAgent;
    private String detalle;
    private LocalDateTime timestamp;
}
