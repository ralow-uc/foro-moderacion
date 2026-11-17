package com.foro.moderacion.service;

import com.foro.moderacion.dto.LevantamientoRequest;
import com.foro.moderacion.dto.SancionRequest;
import com.foro.moderacion.dto.SancionResponse;
import com.foro.moderacion.model.TipoContenido;
import com.foro.moderacion.security.UsuarioAutenticado;

import java.util.List;

/**
 * Contrato de la capa de servicio de las sanciones.
 *
 * Las operaciones que cambian el estado del contenido reciben la cabecera
 * Authorization del moderador para reenviarla a foro-temas.
 */
public interface SancionService {

    /** Banea el contenido en foro-temas, registra la sancion y acepta los reportes pendientes. */
    SancionResponse sancionar(SancionRequest solicitud, UsuarioAutenticado moderador, String autorizacion);

    /** Restituye el contenido en foro-temas y deja la sancion sin vigencia. Solo ADMIN. */
    SancionResponse levantar(Long id, LevantamientoRequest solicitud, UsuarioAutenticado administrador,
                             String autorizacion);

    List<SancionResponse> listar(Boolean vigente, TipoContenido tipo);

    SancionResponse obtenerPorId(Long id);

    /** Historial de sanciones de un autor. */
    List<SancionResponse> listarPorAutor(Long autorId);
}
