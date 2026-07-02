package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.TipoAula;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class SolicitudTutoriaCreateRequest {

    @NotNull
    private UUID materiaId;

    @NotNull
    private TipoAula tipoAulaSolicitada;

    @NotNull
    private LocalDateTime fechaHoraInicio;

    @NotNull
    @DecimalMin(value = "1.00", message = "duracion_horas debe ser >= 1.00 (RN-14)")
    @DecimalMax(value = "2.00", message = "duracion_horas debe ser <= 2.00 (RN-14)")
    private BigDecimal duracionHoras;
}
