package com.clinica.gestion.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Medicamento dentro de una prescripcion: nombre, dosis y duracion.
 */
public record Medicamento(
        @NotBlank(message = "es obligatorio") String nombre,
        @NotBlank(message = "es obligatoria") String dosis,
        @NotBlank(message = "es obligatoria") String duracion) {
}
