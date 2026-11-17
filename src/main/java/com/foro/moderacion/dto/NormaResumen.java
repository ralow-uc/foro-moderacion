package com.foro.moderacion.dto;

import com.foro.moderacion.model.Gravedad;
import com.foro.moderacion.model.Norma;

/**
 * Datos minimos de la norma citada por un reporte o una sancion.
 */
public record NormaResumen(Long id, String codigo, String nombre, Gravedad gravedad) {

    public static NormaResumen desde(Norma norma) {
        return new NormaResumen(norma.getId(), norma.getCodigo(), norma.getNombre(), norma.getGravedad());
    }
}
