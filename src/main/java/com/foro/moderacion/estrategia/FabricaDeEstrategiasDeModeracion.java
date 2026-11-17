package com.foro.moderacion.estrategia;

import com.foro.moderacion.model.TipoContenido;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Patron FACTORY: entrega la estrategia de moderacion que corresponde a cada
 * tipo de contenido.
 *
 * Spring inyecta todas las implementaciones de EstrategiaDeModeracion y la
 * fabrica las indexa por tipo al arrancar. Si falta la estrategia de algun
 * tipo, o hay dos para el mismo, el servicio no levanta: el error aparece al
 * desplegar y no cuando un moderador intenta banear.
 */
@Component
public class FabricaDeEstrategiasDeModeracion {

    private final Map<TipoContenido, EstrategiaDeModeracion> estrategias = new EnumMap<>(TipoContenido.class);

    public FabricaDeEstrategiasDeModeracion(List<EstrategiaDeModeracion> disponibles) {
        for (EstrategiaDeModeracion estrategia : disponibles) {
            EstrategiaDeModeracion anterior = estrategias.put(estrategia.tipo(), estrategia);
            if (anterior != null) {
                throw new IllegalStateException("Hay dos estrategias para " + estrategia.tipo() + ": "
                        + anterior.getClass().getSimpleName() + " y " + estrategia.getClass().getSimpleName());
            }
        }
        for (TipoContenido tipo : TipoContenido.values()) {
            if (!estrategias.containsKey(tipo)) {
                throw new IllegalStateException("Falta la estrategia de moderacion para " + tipo);
            }
        }
    }

    /** Estrategia del tipo pedido. Nunca devuelve null: el constructor lo garantiza. */
    public EstrategiaDeModeracion para(TipoContenido tipo) {
        return estrategias.get(tipo);
    }
}
