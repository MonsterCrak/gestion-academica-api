package edu.upc.sistema.gestionacademicaapi.enums;

/**
 * Estado del recurso (Anexo A). RESERVADO se usa al bloquear por una reserva de aula;
 * DADO_DE_BAJA retira el recurso del inventario operativo (HU-06).
 */
public enum EstadoRecurso {
    DISPONIBLE,
    PRESTADO,
    RESERVADO,
    MANTENIMIENTO,
    DADO_DE_BAJA
}
