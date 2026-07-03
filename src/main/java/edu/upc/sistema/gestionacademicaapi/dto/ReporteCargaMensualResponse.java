package edu.upc.sistema.gestionacademicaapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteCargaMensualResponse {

    private Long docenteId;
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
        private Long solicitudId;
        private Long materiaId;
        private String materiaCodigo;
        private String materiaNombre;
        private LocalDateTime fechaHoraInicio;
        private LocalDateTime fechaHoraFin;
        private BigDecimal horasEfectivas;
        private BigDecimal horasRectificadas;
    }
}