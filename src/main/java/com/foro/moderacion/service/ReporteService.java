package com.foro.moderacion.service;

import com.foro.moderacion.dto.ReporteRequest;
import com.foro.moderacion.dto.ReporteResponse;
import com.foro.moderacion.model.EstadoReporte;
import com.foro.moderacion.model.TipoContenido;
import com.foro.moderacion.security.UsuarioAutenticado;

import java.util.List;

/**
 * Contrato de la capa de servicio de los reportes.
 */
public interface ReporteService {

    /** Un usuario autenticado reporta un tema o un comentario. */
    ReporteResponse reportar(ReporteRequest solicitud, UsuarioAutenticado reportante);

    /** Bandeja del moderador. Sin estado devuelve todos, del mas reciente al mas antiguo. */
    List<ReporteResponse> listar(EstadoReporte estado);

    /** Reportes que hizo el propio usuario, para que vea en que quedaron. */
    List<ReporteResponse> listarPropios(UsuarioAutenticado reportante);

    ReporteResponse obtenerPorId(Long id);

    /** El moderador revisa y concluye que el contenido no incumple las normas. */
    ReporteResponse descartar(Long id, UsuarioAutenticado moderador);

    /**
     * Marca como ACEPTADOS todos los reportes pendientes sobre un contenido.
     * Lo usa el servicio de sanciones al banear. Devuelve cuantos cerro.
     */
    int aceptarPendientesSobre(TipoContenido tipo, Long contenidoId, UsuarioAutenticado moderador);
}
