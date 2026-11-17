package com.foro.moderacion.dto;

import com.foro.moderacion.model.TipoContenido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Orden de un moderador para banear un tema o un comentario.
 *
 * reporteId es opcional: si la sancion responde a un reporte se indica, y si el
 * moderador actua de oficio se omite. En ambos casos, todos los reportes
 * pendientes sobre ese contenido quedan ACEPTADOS.
 */
public record SancionRequest(
        @NotNull(message = "Indica el tipo de contenido: TEMA o COMENTARIO")
        TipoContenido tipoContenido,

        @NotNull(message = "Indica el id del contenido a sancionar")
        @Positive(message = "El id del contenido debe ser positivo")
        Long contenidoId,

        @NotNull(message = "Indica la norma incumplida")
        Long normaId,

        Long reporteId,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(min = 10, max = 400, message = "El motivo debe tener entre 10 y 400 caracteres")
        String motivo) {
}
