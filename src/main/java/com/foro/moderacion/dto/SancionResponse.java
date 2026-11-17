package com.foro.moderacion.dto;

import com.foro.moderacion.model.Sancion;
import com.foro.moderacion.model.TipoContenido;

import java.time.LocalDateTime;

/**
 * Sancion devuelta por la API.
 *
 * reportesAceptados solo se informa al crear la sancion: cuantos reportes
 * pendientes sobre el mismo contenido quedaron cerrados como ACEPTADOS.
 */
public record SancionResponse(
        Long id,
        TipoContenido tipoContenido,
        Long contenidoId,
        String tituloContenido,
        Long autorId,
        String autorNombre,
        NormaResumen norma,
        Long reporteId,
        String motivo,
        String moderadorNombre,
        boolean vigente,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaLevantamiento,
        String levantadaPor,
        String motivoLevantamiento,
        Integer reportesAceptados) {

    public static SancionResponse desde(Sancion sancion) {
        return desde(sancion, null);
    }

    public static SancionResponse desde(Sancion sancion, Integer reportesAceptados) {
        return new SancionResponse(
                sancion.getId(),
                sancion.getTipoContenido(),
                sancion.getContenidoId(),
                sancion.getTituloContenido(),
                sancion.getAutorId(),
                sancion.getAutorNombre(),
                NormaResumen.desde(sancion.getNorma()),
                sancion.getReporte() == null ? null : sancion.getReporte().getId(),
                sancion.getMotivo(),
                sancion.getModeradorNombre(),
                sancion.isVigente(),
                sancion.getFechaCreacion(),
                sancion.getFechaLevantamiento(),
                sancion.getLevantadaPor(),
                sancion.getMotivoLevantamiento(),
                reportesAceptados);
    }
}
