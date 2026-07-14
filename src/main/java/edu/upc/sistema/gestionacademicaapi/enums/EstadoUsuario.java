package edu.upc.sistema.gestionacademicaapi.enums;

/**
 * Estado operativo de la cuenta (Anexo A del TF).
 * ACTIVO: puede operar. BLOQUEADO: penalizacion o deuda vigente. INACTIVO: dado de baja.
 */
public enum EstadoUsuario {
    ACTIVO,
    INACTIVO,
    BLOQUEADO
}
