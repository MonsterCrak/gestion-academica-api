package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.EstadoRecurso;
import edu.upc.sistema.gestionacademicaapi.enums.TipoMovilidad;
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
public class RecursoResponse {

    private Long id;
    private Long categoriaId;
    private String numeroSerie;
    private String codigoInventario;
    private String nombre;
    private TipoMovilidad tipoMovilidad;
    private Long espacioActualId;
    private EstadoRecurso estado;
    private Boolean requiereUbicacionFisica;
}