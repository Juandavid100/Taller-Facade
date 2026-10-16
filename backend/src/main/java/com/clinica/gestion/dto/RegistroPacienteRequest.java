package com.clinica.gestion.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos que envia el frontend para registrar un paciente.
 */
public record RegistroPacienteRequest(
        @NotBlank(message = "es obligatorio") String documento,
        @NotBlank(message = "es obligatorio") String nombres,
        @NotBlank(message = "es obligatorio") String apellidos,
        @NotBlank(message = "es obligatorio") @Email(message = "no es valido") String email,
        String telefono,
        LocalDate fechaNacimiento,
        @NotBlank(message = "es obligatoria") @Size(min = 6, message = "debe tener minimo 6 caracteres") String password,
        List<String> alergias) {
}
