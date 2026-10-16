package com.clinica.gestion.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Solicitud de examenes: HEMOGRAMA, GLICEMIA, PERFIL_LIPIDICO.
 */
public record LaboratorioRequest(
        @NotNull(message = "es obligatorio") Long pacienteId,
        @NotEmpty(message = "debe tener al menos un examen") List<String> examenes) {
}
