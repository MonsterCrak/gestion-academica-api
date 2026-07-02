package edu.upc.sistema.gestionacademicaapi.dto;

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
public class CategoriaResponse {

    private UUID id;
    private String nombreCategoria;
    private Integer maxItemsPorAlumno;
    private Integer tiempoMaximoHoras;
}
