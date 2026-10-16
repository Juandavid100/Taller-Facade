package com.clinica.gestion.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Paciente registrado en el portal. La contrasena nunca se devuelve en el JSON.
 */
public record Paciente(
        Long id,
        String documento,
        String nombres,
        String apellidos,
        String email,
        String telefono,
        LocalDate fechaNacimiento,
        @JsonIgnore String passwordHash) {

    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }
}
