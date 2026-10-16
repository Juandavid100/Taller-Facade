package com.clinica.gestion.dto;

import java.util.List;

import com.clinica.gestion.model.Medicamento;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Solicitud para generar una prescripcion.
 */
public record PrescripcionRequest(
        @NotNull(message = "es obligatorio") Long pacienteId,
        @NotEmpty(message = "debe tener al menos un medicamento") List<@Valid Medicamento> medicamentos,
        String indicaciones) {
}
