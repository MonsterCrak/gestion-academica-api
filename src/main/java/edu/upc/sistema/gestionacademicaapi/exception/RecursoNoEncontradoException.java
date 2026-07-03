package edu.upc.sistema.gestionacademicaapi.exception;

import lombok.Getter;

@Getter
public class RecursoNoEncontradoException extends RuntimeException {

    private final String recurso;
    private final Long id;

    public RecursoNoEncontradoException(String recurso, Long id) {
        super("No se encontro " + recurso + " con id " + id);
        this.recurso = recurso;
        this.id = id;
    }

    public RecursoNoEncontradoException(String recurso, String criterio) {
        super("No se encontro " + recurso + " con " + criterio);
        this.recurso = recurso;
        this.id = null;
    }
}