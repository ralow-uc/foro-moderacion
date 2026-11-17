package com.foro.moderacion.dto;

import com.foro.moderacion.model.Gravedad;
import com.foro.moderacion.model.Norma;

import java.time.LocalDateTime;

/**
 * Norma devuelta por la API. El controlador nunca expone la entidad JPA.
 */
public record NormaResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        Gravedad gravedad,
        boolean activo,
        LocalDateTime fechaCreacion) {

    public static NormaResponse desde(Norma norma) {
        return new NormaResponse(
                norma.getId(),
                norma.getCodigo(),
                norma.getNombre(),
                norma.getDescripcion(),
                norma.getGravedad(),
                norma.isActivo(),
                norma.getFechaCreacion());
    }
}
