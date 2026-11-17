package com.foro.moderacion.estrategia;

import com.foro.moderacion.cliente.ClienteForoTemas;
import com.foro.moderacion.cliente.ContenidoModerado;
import com.foro.moderacion.cliente.OrdenDeModeracion;
import com.foro.moderacion.model.TipoContenido;
import org.springframework.stereotype.Component;

/**
 * Estrategia para COMENTARIOS. Banear un comentario lo oculta del detalle del
 * tema y del conteo de comentarios; el tema sigue visible.
 */
@Component
public class ModeracionDeComentario implements EstrategiaDeModeracion {

    private static final String RUTA = "/api/moderacion/comentarios/{id}";

    private final ClienteForoTemas cliente;

    public ModeracionDeComentario(ClienteForoTemas cliente) {
        this.cliente = cliente;
    }

    @Override
    public TipoContenido tipo() {
        return TipoContenido.COMENTARIO;
    }

    @Override
    public ContenidoModerado banear(Long contenidoId, String motivo, String autorizacion) {
        return cliente.moderar(RUTA, "el comentario", contenidoId, new OrdenDeModeracion(true, motivo), autorizacion);
    }

    @Override
    public ContenidoModerado restituir(Long contenidoId, String autorizacion) {
        return cliente.moderar(RUTA, "el comentario", contenidoId, new OrdenDeModeracion(false, null), autorizacion);
    }
}
