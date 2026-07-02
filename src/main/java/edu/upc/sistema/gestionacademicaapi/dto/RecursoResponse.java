package edu.upc.sistema.gestionacademicaapi.dto;

import edu.upc.sistema.gestionacademicaapi.enums.EstadoRecurso;
import edu.upc.sistema.gestionacademicaapi.enums.TipoMovilidad;
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
public class RecursoResponse {

    private UUID id;
    private UUID categoriaId;
    private String numeroSerie;
    private String codigoInventario;
    private String nombre;
    private TipoMovilidad tipoMovilidad;
    private UUID espacioActualId;
    private EstadoRecurso estado;
    private Boolean requiereUbicacionFisica;
}
