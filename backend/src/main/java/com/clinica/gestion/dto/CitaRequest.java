package com.clinica.gestion.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Solicitud para agendar una cita. medicoId es opcional:
 * si no llega, la agenda asigna el primer medico libre de la especialidad.
 * fecha en formato ISO: 2026-10-20T09:00
 */
public record CitaRequest(
        @NotNull(message = "es obligatorio") Long pacienteId,
        @NotBlank(message = "es obligatoria") String especialidad,
        @NotNull(message = "es obligatoria") LocalDateTime fecha,
        Long medicoId) {
}
