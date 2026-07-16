package edu.upc.sistema.gestionacademicaapi.enums;

/**
 * Forma en que el usuario puede resolver/levantar una penalización (HU-20).
 */
public enum ModoResolucion {
    /** Monto de dinero a pagar en la siguiente pensión (estudiante). */
    PAGO_PENSION,
    /** Descuento en haberes/planilla (docente). */
    DESCUENTO_HABERES,
    /** Solo tiempo sin uso de los recursos de la universidad (sin costo). */
    SUSPENSION_RECURSOS
}
