package com.foro.moderacion.cliente;

/**
 * Cuerpo que espera foro-temas en PUT /api/moderacion/{temas|comentarios}/{id}.
 */
public record OrdenDeModeracion(boolean baneado, String motivo) {
}
