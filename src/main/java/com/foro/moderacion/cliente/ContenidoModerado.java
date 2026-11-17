package com.foro.moderacion.cliente;

/**
 * Respuesta de foro-temas despues de aplicar una orden de moderacion
 * (ModeracionResponse en ese servicio). Trae el autor del contenido, que este
 * microservicio guarda en la sancion para poder consultar reincidencias.
 */
public record ContenidoModerado(
        String tipo,
        Long id,
        String titulo,
        Long autorId,
        String autorNombre,
        boolean baneado,
        String motivo) {
}
