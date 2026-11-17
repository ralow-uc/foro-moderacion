package com.foro.moderacion.estrategia;

import com.foro.moderacion.cliente.ContenidoModerado;
import com.foro.moderacion.model.TipoContenido;

/**
 * Patron STRATEGY: como se banea y se restituye cada tipo de contenido.
 *
 * El servicio de sanciones no pregunta "si es tema haz esto, si es comentario
 * haz lo otro": le pide a la fabrica la estrategia del tipo y la ejecuta. Para
 * moderar un tipo nuevo (por ejemplo, perfiles de usuario) basta con agregar
 * otra implementacion anotada con @Component; el servicio no cambia
 * (principio abierto/cerrado).
 */
public interface EstrategiaDeModeracion {

    /** Tipo de contenido que atiende esta estrategia. */
    TipoContenido tipo();

    /** Retira el contenido del foro. */
    ContenidoModerado banear(Long contenidoId, String motivo, String autorizacion);

    /** Vuelve a mostrar el contenido cuando se levanta la sancion. */
    ContenidoModerado restituir(Long contenidoId, String autorizacion);
}
