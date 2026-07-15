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
import java.util.List;

/** GET /{id} enriquecido: incluye los confirmados para evitar la llamada aparte a /confirmaciones. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudTutoriaDetalleResponse {

    private Long id;
    private Long creadorId;
    private Long materiaId;
    private Long espacioAsignadoId;
    private Long docenteAsignadoId;
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
    private List<ConfirmacionAlumnoResponse> confirmados;
}
