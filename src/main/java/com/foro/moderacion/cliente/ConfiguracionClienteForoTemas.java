package com.foro.moderacion.cliente;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Crea el RestClient con el que este servicio habla con foro-temas.
 *
 * La URL llega por FORO_TEMAS_URL: en local es http://localhost:8082 y en
 * Docker es el nombre del contenedor (http://foro-temas:8082). Los tiempos de
 * espera son cortos a proposito: si foro-temas no contesta, el moderador
 * recibe un 503 en segundos en lugar de quedarse esperando.
 */
@Configuration
public class ConfiguracionClienteForoTemas {

    @Bean
    public RestClient restClientForoTemas(RestClient.Builder builder,
                                          @Value("${foro.temas.url}") String urlForoTemas) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(3));
        fabrica.setReadTimeout(Duration.ofSeconds(10));
        return builder.baseUrl(urlForoTemas).requestFactory(fabrica).build();
    }
}
