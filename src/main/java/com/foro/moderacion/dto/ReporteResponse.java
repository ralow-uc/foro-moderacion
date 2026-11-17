package com.foro.moderacion.dto;

import com.foro.moderacion.model.EstadoReporte;
import com.foro.moderacion.model.Reporte;
import com.foro.moderacion.model.TipoContenido;

import java.time.LocalDateTime;

/**
 * Reporte devuelto por la API.
 */
public record ReporteResponse(
        Long id,
        TipoContenido tipoContenido,
        Long contenidoId,
        NormaResumen norma,
        Long reportanteId,
        String reportanteNombre,
        String detalle,
        EstadoReporte estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaResolucion,
        String resueltoPor) {

    public static ReporteResponse desde(Reporte reporte) {
        return new ReporteResponse(
                reporte.getId(),
                reporte.getTipoContenido(),
                reporte.getContenidoId(),
                NormaResumen.desde(reporte.getNorma()),
                reporte.getReportanteId(),
                reporte.getReportanteNombre(),
                reporte.getDetalle(),
                reporte.getEstado(),
                reporte.getFechaCreacion(),
                reporte.getFechaResolucion(),
                reporte.getResueltoPor());
    }
}
