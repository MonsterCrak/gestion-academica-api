package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.ModalidadAsignacion;
import edu.upc.sistema.gestionacademicaapi.enums.TipoAula;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteMateriaResponse {

    private UUID id;
    private UUID docenteId;
    private UUID materiaId;
    private TipoAula tipoAulaRequerida;
    private ModalidadAsignacion modalidadAsignacion;
    private LocalDateTime fechaAlta;
    private Boolean activo;
}
