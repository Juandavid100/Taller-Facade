package com.clinica.gestion.dto;

import java.util.List;

import com.clinica.gestion.model.Cita;
import com.clinica.gestion.model.Consulta;
import com.clinica.gestion.model.Paciente;
import com.clinica.gestion.model.Prescripcion;
import com.clinica.gestion.model.ResultadoLaboratorio;

/**
 * Objeto unico que arma la fachada juntando la informacion de los 5 subsistemas:
 * citas pasadas + proximas, consultas, alergias, prescripciones y laboratorios.
 */
public record HistoriaCompleta(
        Paciente paciente,
        List<String> alergias,
        List<Cita> proximasCitas,
        List<Cita> citasPasadas,
        List<Consulta> consultas,
        List<Prescripcion> prescripciones,
        List<ResultadoLaboratorio> laboratorios) {
}
