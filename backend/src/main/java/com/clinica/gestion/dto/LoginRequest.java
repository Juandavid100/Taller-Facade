package com.clinica.gestion.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Inicio de sesion con documento y contrasena.
 */
public record LoginRequest(
        @NotBlank(message = "es obligatorio") String documento,
        @NotBlank(message = "es obligatoria") String password) {
}
