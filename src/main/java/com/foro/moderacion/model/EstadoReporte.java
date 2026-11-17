package com.foro.moderacion.model;

/**
 * Ciclo de vida de un reporte:
 *   PENDIENTE  -> lo creo un usuario y espera revision.
 *   ACEPTADO   -> un moderador lo encontro fundado y sanciono el contenido.
 *   DESCARTADO -> un moderador lo reviso y el contenido no incumple las normas.
 */
public enum EstadoReporte {
    PENDIENTE,
    ACEPTADO,
    DESCARTADO
}
