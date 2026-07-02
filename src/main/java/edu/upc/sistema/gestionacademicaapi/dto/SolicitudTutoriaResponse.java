package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.EstadoSolicitud;
import edu.upc.sistema.gestionacademicaapi.enums.TipoAula;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudTutoriaResponse {

    private UUID id;
    private UUID creadorId;
    private UUID materiaId;
    private UUID espacioAsignadoId;
    private UUID docenteAsignadoId;
    private TipoAula tipoAulaSolicitada;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private BigDecimal duracionHoras;
    private String tokenInvitacion;
    private LocalDateTime fechaExpiracionToken;
    private EstadoSolicitud estado;
    private Boolean docenteConfirmoRealizacion;
    private LocalDateTime docenteConfirmoRealizacionEn;
    private Integer totalConfirmados;
}
