package edu.upc.sistema.gestionacademicaapi.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class RecursoNoEncontradoException extends RuntimeException {

    private final String recurso;
    private final UUID id;

    public RecursoNoEncontradoException(String recurso, UUID id) {
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
