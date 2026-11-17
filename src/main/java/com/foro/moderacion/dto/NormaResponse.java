package com.foro.moderacion.dto;

import com.foro.moderacion.model.Norma;

import java.time.LocalDateTime;

/**
 * Norma devuelto por la API. El controlador nunca expone la entidad JPA.
 */
public record NormaResponse(
        Long id,
        String nombre,
        String descripcion,
        boolean activo,
        LocalDateTime fechaCreacion) {

    public static NormaResponse desde(Norma entidad) {
        return new NormaResponse(
                entidad.getId(),
                entidad.getNombre(),
                entidad.getDescripcion(),
                entidad.isActivo(),
                entidad.getFechaCreacion());
    }
}
