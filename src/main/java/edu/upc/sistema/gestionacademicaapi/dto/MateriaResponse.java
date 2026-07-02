package edu.upc.sistema.gestionacademicaapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MateriaResponse {

    private Long id;
    private String codigo;
    private String nombre;
    private String departamento;
}