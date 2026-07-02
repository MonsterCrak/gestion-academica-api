package edu.upc.sistema.gestionacademicaapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteCargaMensualResponse {

    private UUID docenteId;
    private String identificadorDocente;
    private String nombreDocente;
    private Integer anio;
    private Integer mes;
    private BigDecimal totalHoras;
    private List<DetalleCarga> detalle;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetalleCarga {
        private UUID solicitudId;
        private UUID materiaId;
        private String materiaCodigo;
        private String materiaNombre;
        private java.time.LocalDateTime fechaHoraInicio;
        private java.time.LocalDateTime fechaHoraFin;
        private BigDecimal horasEfectivas;
        private BigDecimal horasRectificadas;
    }
}
