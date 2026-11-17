package com.foro.moderacion.cliente;

import com.foro.moderacion.exception.OperacionNoPermitidaException;
import com.foro.moderacion.exception.RecursoNoEncontradoException;
import com.foro.moderacion.exception.ServicioNoDisponibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Pruebas del adaptador HTTP hacia foro-temas con un servidor simulado: que
 * peticion arma y como traduce cada codigo de respuesta.
 */
class ClienteForoTemasTest {

    private static final String RUTA = "/api/moderacion/temas/{id}";
    private static final String TOKEN = "Bearer token-de-la-moderadora";

    private MockRestServiceServer servidor;
    private ClienteForoTemas cliente;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://foro-temas.prueba");
        servidor = MockRestServiceServer.bindTo(builder).build();
        cliente = new ClienteForoTemas(builder.build());
    }

    @Test
    @DisplayName("Envia PUT con el JWT del moderador y la orden, y lee la respuesta")
    void enviaLaOrdenYLeeLaRespuesta() {
        servidor.expect(requestTo("http://foro-temas.prueba/api/moderacion/temas/7"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header("Authorization", TOKEN))
                .andExpect(content().json("{\"baneado\":true,\"motivo\":\"N2 - Spam\"}"))
                .andRespond(withSuccess("""
                        {"tipo":"TEMA","id":7,"titulo":"Compra cubos","autorId":4,
                         "autorNombre":"yiheng","baneado":true,"motivo":"N2 - Spam"}
                        """, MediaType.APPLICATION_JSON));

        ContenidoModerado resultado = cliente.moderar(RUTA, "el tema", 7L,
                new OrdenDeModeracion(true, "N2 - Spam"), TOKEN);

        assertThat(resultado.autorNombre()).isEqualTo("yiheng");
        assertThat(resultado.baneado()).isTrue();
        servidor.verify();
    }

    @Test
    @DisplayName("Un 404 de foro-temas se traduce en RecursoNoEncontradoException")
    void traduce404() {
        servidor.expect(requestTo("http://foro-temas.prueba/api/moderacion/temas/99"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> cliente.moderar(RUTA, "el tema", 99L, new OrdenDeModeracion(true, "x"), TOKEN))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("el tema 99");
    }

    @Test
    @DisplayName("Un 403 de foro-temas se traduce en OperacionNoPermitidaException")
    void traduce403() {
        servidor.expect(requestTo("http://foro-temas.prueba/api/moderacion/temas/7"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> cliente.moderar(RUTA, "el tema", 7L, new OrdenDeModeracion(true, "x"), TOKEN))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }

    @Test
    @DisplayName("Un 500 de foro-temas se traduce en ServicioNoDisponibleException")
    void traduce500() {
        servidor.expect(requestTo("http://foro-temas.prueba/api/moderacion/temas/7"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> cliente.moderar(RUTA, "el tema", 7L, new OrdenDeModeracion(true, "x"), TOKEN))
                .isInstanceOf(ServicioNoDisponibleException.class);
    }
}
