package edu.upc.sistema.gestionacademicaapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Progreso de la demanda de tutoría por asignatura (HU-16). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandaMateriaResponse {

    private Long materiaId;
    private String codigo;
    private String nombre;
    private String departamento;
    private long inscritos;
    private int quorum;
    private boolean yaInscrito;
}
