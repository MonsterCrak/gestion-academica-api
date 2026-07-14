package edu.upc.sistema.gestionacademicaapi.enums;

/**
 * Estado de una sesión de tutoría consolidada (HU-17/18).
 * EN_ESPERA_RECURSOS: se alcanzó el quórum pero falta docente o aula (intervención manual, CP-15).
 */
public enum EstadoSesionTutoria {
    CONFIRMADA,
    EN_ESPERA_RECURSOS,
    CANCELADA,
    REALIZADA
}
