package com.foro.moderacion.cliente;

import com.foro.moderacion.exception.OperacionNoPermitidaException;
import com.foro.moderacion.exception.RecursoNoEncontradoException;
import com.foro.moderacion.exception.ServicioNoDisponibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

/**
 * Patron ADAPTER: traduce la API HTTP de foro-temas a llamadas Java y sus
 * codigos de error a las excepciones propias de este servicio.
 *
 *   404 de foro-temas      -> RecursoNoEncontradoException   (404 aqui)
 *   401 / 403 de foro-temas -> OperacionNoPermitidaException (403 aqui)
 *   otro error, sin red o
 *   respuesta ilegible     -> ServicioNoDisponibleException  (503 aqui)
 *
 * Asi el resto del codigo no sabe que hay HTTP de por medio: las estrategias de
 * moderacion piden "banear este tema" y reciben un ContenidoModerado.
 *
 * El JWT del moderador se reenvia tal cual: foro-temas vuelve a comprobar que
 * tenga rol MODERADOR o ADMIN. No hay un usuario tecnico con mas privilegios.
 */
@Component
public class ClienteForoTemas {

    private static final Logger log = LoggerFactory.getLogger(ClienteForoTemas.class);

    private final RestClient restClient;

    public ClienteForoTemas(@Qualifier("restClientForoTemas") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Envia una orden de moderacion a foro-temas.
     *
     * @param ruta          plantilla de la ruta, ej. /api/moderacion/temas/{id}
     * @param descripcion   "el tema" o "el comentario", para los mensajes de error
     * @param autorizacion  cabecera Authorization del moderador ("Bearer ...")
     */
    public ContenidoModerado moderar(String ruta, String descripcion, Long contenidoId,
                                     OrdenDeModeracion orden, String autorizacion) {
        log.debug("PUT foro-temas {} id={} baneado={}", ruta, contenidoId, orden.baneado());
        ContenidoModerado contenido;
        try {
            contenido = restClient.put()
                    .uri(ruta, contenidoId)
                    .header(HttpHeaders.AUTHORIZATION, autorizacion)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(orden)
                    .retrieve()
                    .onStatus(estado -> estado.value() == 404, (peticion, respuesta) -> {
                        throw new RecursoNoEncontradoException(
                                "No existe " + descripcion + " " + contenidoId + " en foro-temas");
                    })
                    .onStatus(estado -> estado.value() == 401 || estado.value() == 403, (peticion, respuesta) -> {
                        throw new OperacionNoPermitidaException(
                                "foro-temas rechazo la credencial: se requiere rol MODERADOR o ADMIN");
                    })
                    .onStatus(HttpStatusCode::isError, (peticion, respuesta) -> {
                        throw new ServicioNoDisponibleException(
                                "foro-temas respondio " + respuesta.getStatusCode().value());
                    })
                    .body(ContenidoModerado.class);
        } catch (ResourceAccessException e) {
            throw new ServicioNoDisponibleException("No se pudo contactar a foro-temas", e);
        } catch (RestClientException e) {
            // Respondio algo que no es la API de foro-temas (cuerpo vacio o que no es
            // JSON): por ejemplo un proxy o la red de Docker con el contenedor caido.
            throw new ServicioNoDisponibleException("foro-temas devolvio una respuesta ilegible", e);
        }
        if (contenido == null || contenido.id() == null) {
            throw new ServicioNoDisponibleException("foro-temas respondio sin el contenido moderado");
        }
        return contenido;
    }
}
