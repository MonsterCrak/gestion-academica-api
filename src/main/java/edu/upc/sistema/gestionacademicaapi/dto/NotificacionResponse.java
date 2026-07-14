package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.EstadoNotificacion;
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
public class NotificacionResponse {

    private Long id;
    private String tipo;
    private String asunto;
    private String cuerpo;
    private EstadoNotificacion estadoEnvio;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnvio;
    private Integer intentos;
}
