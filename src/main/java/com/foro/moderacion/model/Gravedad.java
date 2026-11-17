package com.foro.moderacion.model;

/**
 * Gravedad de una norma del foro. Orienta al moderador sobre la sancion:
 * una falta LEVE suele terminar en advertencia, una CRITICA en baneo inmediato.
 */
public enum Gravedad {
    LEVE,
    GRAVE,
    CRITICA
}
