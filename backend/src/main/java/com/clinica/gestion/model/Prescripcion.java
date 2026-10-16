package com.clinica.gestion.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Prescripcion medica generada para un paciente.
 */
public record Prescripcion(
        Long id,
        Long pacienteId,
        LocalDateTime fecha,
        List<Medicamento> medicamentos,
        String indicaciones) {
}
