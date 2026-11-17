package com.foro.moderacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de moderacion del foro: normas, reportes y sanciones sobre temas y comentarios.
 *
 * Generado con el arquetipo com.foro:microservicio-arquetipo. La estructura en
 * capas (controller, service, repository, model, dto) y la seguridad por JWT
 * son las mismas en todos los microservicios del foro.
 */
@SpringBootApplication
public class ForoModeracionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForoModeracionApplication.class, args);
    }
}
