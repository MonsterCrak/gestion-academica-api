package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.ModalidadAsignacion;
import edu.upc.sistema.gestionacademicaapi.enums.TipoAula;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteMateriaCreateRequest {

    @NotNull
    private UUID materiaId;

    @NotNull
    private TipoAula tipoAulaRequerida;

    @NotNull
    private ModalidadAsignacion modalidadAsignacion;
}
