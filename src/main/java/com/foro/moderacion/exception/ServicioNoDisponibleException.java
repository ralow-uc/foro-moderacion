package com.foro.moderacion.exception;

/**
 * foro-temas no respondio o respondio con un error inesperado. Se responde 503
 * para que el cliente sepa que puede reintentar: la sancion no se registro.
 */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
