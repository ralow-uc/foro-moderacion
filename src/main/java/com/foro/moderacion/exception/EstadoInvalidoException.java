package com.foro.moderacion.exception;

/**
 * La operacion no es valida en el estado actual del recurso: descartar un
 * reporte ya resuelto, sancionar dos veces el mismo contenido, levantar una
 * sancion que ya no esta vigente. Se responde 409.
 */
public class EstadoInvalidoException extends RuntimeException {

    public EstadoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
