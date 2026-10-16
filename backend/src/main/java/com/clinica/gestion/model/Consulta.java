package com.clinica.gestion.model;

import java.time.LocalDateTime;

/**
 * Consulta previa registrada en la historia clinica.
 */
public record Consulta(
        Long id,
        Long pacienteId,
        LocalDateTime fecha,
        String medico,
        String especialidad,
        String motivo,
        String diagnostico) {
}
