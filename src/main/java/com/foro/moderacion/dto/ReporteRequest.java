package com.foro.moderacion.dto;

import com.foro.moderacion.model.TipoContenido;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Reporte que envia un usuario autenticado. Quien reporta no viaja en el
 * cuerpo: se toma del JWT, igual que el autor de un tema en foro-temas.
 */
public record ReporteRequest(
        @NotNull(message = "Indica el tipo de contenido: TEMA o COMENTARIO")
        TipoContenido tipoContenido,

        @NotNull(message = "Indica el id del contenido reportado")
        @Positive(message = "El id del contenido debe ser positivo")
        Long contenidoId,

        @NotNull(message = "Indica la norma que se incumple")
        Long normaId,

        @Size(max = 500, message = "El detalle no puede superar los 500 caracteres")
        String detalle) {
}
