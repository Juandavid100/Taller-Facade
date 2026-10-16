package com.clinica.gestion.dto;

import java.util.List;

/**
 * Medico con los horarios que tiene libres en el dia consultado.
 */
public record MedicoDisponible(
        Long id,
        String nombre,
        String especialidad,
        List<String> horariosDisponibles) {
}
