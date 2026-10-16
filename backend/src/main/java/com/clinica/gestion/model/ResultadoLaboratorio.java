package com.clinica.gestion.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Resultado simulado de un examen de laboratorio.
 */
public record ResultadoLaboratorio(
        Long id,
        Long pacienteId,
        String examen,
        String nombreExamen,
        LocalDateTime fecha,
        List<ParametroLaboratorio> parametros) {

    public boolean todoNormal() {
        return parametros.stream().allMatch(ParametroLaboratorio::dentroDeRango);
    }
}
