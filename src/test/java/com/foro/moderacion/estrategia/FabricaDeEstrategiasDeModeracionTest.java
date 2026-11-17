package com.foro.moderacion.estrategia;

import com.foro.moderacion.cliente.ClienteForoTemas;
import com.foro.moderacion.model.TipoContenido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Pruebas unitarias de la fabrica de estrategias (patron Factory + Strategy),
 * sin Spring: se arma con las estrategias a mano.
 */
class FabricaDeEstrategiasDeModeracionTest {

    private final ClienteForoTemas cliente = mock(ClienteForoTemas.class);

    @Test
    @DisplayName("Entrega la estrategia que corresponde a cada tipo de contenido")
    void entregaLaEstrategiaDeCadaTipo() {
        FabricaDeEstrategiasDeModeracion fabrica = new FabricaDeEstrategiasDeModeracion(
                List.of(new ModeracionDeTema(cliente), new ModeracionDeComentario(cliente)));

        assertThat(fabrica.para(TipoContenido.TEMA)).isInstanceOf(ModeracionDeTema.class);
        assertThat(fabrica.para(TipoContenido.COMENTARIO)).isInstanceOf(ModeracionDeComentario.class);
    }

    @Test
    @DisplayName("No arranca si a algun tipo le falta su estrategia")
    void fallaSiFaltaUnaEstrategia() {
        assertThatThrownBy(() -> new FabricaDeEstrategiasDeModeracion(List.of(new ModeracionDeTema(cliente))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("COMENTARIO");
    }

    @Test
    @DisplayName("No arranca si hay dos estrategias para el mismo tipo")
    void fallaSiHayEstrategiasDuplicadas() {
        assertThatThrownBy(() -> new FabricaDeEstrategiasDeModeracion(List.of(
                new ModeracionDeTema(cliente), new ModeracionDeTema(cliente), new ModeracionDeComentario(cliente))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dos estrategias");
    }
}
