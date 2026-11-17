package com.foro.moderacion.estrategia;

import com.foro.moderacion.cliente.ClienteForoTemas;
import com.foro.moderacion.cliente.ContenidoModerado;
import com.foro.moderacion.cliente.OrdenDeModeracion;
import com.foro.moderacion.model.TipoContenido;
import org.springframework.stereotype.Component;

/**
 * Estrategia para TEMAS. Banear un tema lo saca de las busquedas de foro-temas
 * y su detalle pasa a responder 410; sus comentarios quedan ocultos con el.
 */
@Component
public class ModeracionDeTema implements EstrategiaDeModeracion {

    private static final String RUTA = "/api/moderacion/temas/{id}";

    private final ClienteForoTemas cliente;

    public ModeracionDeTema(ClienteForoTemas cliente) {
        this.cliente = cliente;
    }

    @Override
    public TipoContenido tipo() {
        return TipoContenido.TEMA;
    }

    @Override
    public ContenidoModerado banear(Long contenidoId, String motivo, String autorizacion) {
        return cliente.moderar(RUTA, "el tema", contenidoId, new OrdenDeModeracion(true, motivo), autorizacion);
    }

    @Override
    public ContenidoModerado restituir(Long contenidoId, String autorizacion) {
        return cliente.moderar(RUTA, "el tema", contenidoId, new OrdenDeModeracion(false, null), autorizacion);
    }
}
