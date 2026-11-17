package com.foro.moderacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Motivo por el que un ADMIN levanta una sancion (por ejemplo, tras una
 * apelacion del autor).
 */
public record LevantamientoRequest(
        @NotBlank(message = "El motivo del levantamiento es obligatorio")
        @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
        String motivo) {
}
