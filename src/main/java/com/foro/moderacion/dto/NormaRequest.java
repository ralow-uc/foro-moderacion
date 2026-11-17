package com.foro.moderacion.dto;

import com.foro.moderacion.model.Gravedad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para crear o actualizar una norma del foro.
 */
public record NormaRequest(
        @NotBlank(message = "El codigo es obligatorio")
        @Pattern(regexp = "(?i)N\\d{1,3}", message = "El codigo debe tener la forma N1, N2, ... N999")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,

        @NotBlank(message = "La descripcion es obligatoria")
        @Size(max = 400, message = "La descripcion no puede superar los 400 caracteres")
        String descripcion,

        @NotNull(message = "La gravedad es obligatoria: LEVE, GRAVE o CRITICA")
        Gravedad gravedad,

        @NotNull(message = "Indica si la norma esta activa")
        Boolean activo) {
}
