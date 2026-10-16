package com.clinica.gestion.exception;

import org.springframework.http.HttpStatus;

/**
 * Error de negocio con el codigo HTTP que debe devolver la API.
 */
public class ClinicaException extends RuntimeException {

    private final HttpStatus status;

    public ClinicaException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ClinicaException noEncontrado(String mensaje) {
        return new ClinicaException(HttpStatus.NOT_FOUND, mensaje);
    }

    public static ClinicaException invalido(String mensaje) {
        return new ClinicaException(HttpStatus.BAD_REQUEST, mensaje);
    }

    public static ClinicaException conflicto(String mensaje) {
        return new ClinicaException(HttpStatus.CONFLICT, mensaje);
    }
}
