package com.foro.moderacion.model;

/**
 * Tipo de contenido del foro sobre el que se puede reportar o sancionar.
 *
 * Cada valor tiene su propia estrategia de moderacion (ver el paquete
 * moderacion): agregar un tipo nuevo no obliga a tocar el servicio.
 */
public enum TipoContenido {
    TEMA,
    COMENTARIO
}
