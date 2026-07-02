package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.ModalidadAsignacion;
import edu.upc.sistema.gestionacademicaapi.enums.TipoAula;
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
public class DocenteMateriaResponse {

    private Long id;
    private Long docenteId;
    private Long materiaId;
    private TipoAula tipoAulaRequerida;
    private ModalidadAsignacion modalidadAsignacion;
    private LocalDateTime fechaAlta;
    private Boolean activo;
}