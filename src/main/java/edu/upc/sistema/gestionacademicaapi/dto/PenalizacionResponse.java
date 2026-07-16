package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.ModoResolucion;
import edu.upc.sistema.gestionacademicaapi.enums.OrigenPenalizacion;
import edu.upc.sistema.gestionacademicaapi.enums.TipoPenalizacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenalizacionResponse {

    private Long id;
    private Long usuarioId;
    private String usuarioIdentificador;
    private TipoPenalizacion tipo;
    private OrigenPenalizacion origen;
    private String motivo;
    private ModoResolucion modoResolucion;
    private BigDecimal monto;
    private String instruccionesResolucion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Boolean activa;
    private Long prestamoId;
}
