package com.clinica.gestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.clinica.gestion.exception.ClinicaException;
import com.clinica.gestion.model.Medicamento;
import com.clinica.gestion.model.ParametroLaboratorio;
import com.clinica.gestion.model.ResultadoLaboratorio;
import com.clinica.gestion.service.HistoriaClinicaService;
import com.clinica.gestion.service.LaboratorioService;
import com.clinica.gestion.service.PrescripcionService;

/**
 * Pruebas de los subsistemas de historia clinica, prescripciones y laboratorio.
 */
class SubsistemasClinicosTest {

    @Test
    void detectaAlergiaPorFamiliaDeMedicamento() {
        HistoriaClinicaService historia = new HistoriaClinicaService();
        historia.crearHistoria(1L, List.of("Penicilina"));

        ClinicaException error = assertThrows(ClinicaException.class,
                () -> historia.validarAlergias(1L, List.of("Amoxicilina 500 mg")));
        assertTrue(error.getMessage().contains("Penicilina"));

        historia.validarAlergias(1L, List.of("Acetaminofen 500 mg"));
    }

    @Test
    void registraYOrdenaConsultas() {
        HistoriaClinicaService historia = new HistoriaClinicaService();
        historia.crearHistoria(1L, List.of());
        historia.registrarConsulta(1L, LocalDateTime.now().minusDays(10), "Dra. A", "Medicina General", "Control", "Sano");
        historia.registrarConsulta(1L, LocalDateTime.now().minusDays(1), "Dr. B", "Cardiologia", "Dolor", "Estable");

        assertEquals("Dr. B", historia.consultarConsultas(1L).get(0).medico());
    }

    @Test
    void prescripcionExigeNombreDosisYDuracion() {
        PrescripcionService prescripciones = new PrescripcionService();
        assertThrows(ClinicaException.class,
                () -> prescripciones.generar(1L, List.of(new Medicamento("Loratadina", "", "5 dias")), null));

        prescripciones.generar(1L, List.of(new Medicamento("Loratadina 10 mg", "1 diaria", "5 dias")), null);
        assertEquals(1, prescripciones.consultarPorPaciente(1L).size());
    }

    @Test
    void laboratorioDevuelveValoresConReferencia() {
        LaboratorioService laboratorio = new LaboratorioService();
        List<ResultadoLaboratorio> resultados = laboratorio.solicitar(1L, List.of("hemograma", "glicemia"));

        assertEquals(2, resultados.size());
        assertEquals(4, resultados.get(0).parametros().size());
        ParametroLaboratorio glucosa = resultados.get(1).parametros().get(0);
        assertEquals(70.0, glucosa.minimo());
        assertEquals(glucosa.valor() >= 70 && glucosa.valor() <= 100, glucosa.dentroDeRango());
    }

    @Test
    void laboratorioRechazaExamenDesconocido() {
        LaboratorioService laboratorio = new LaboratorioService();
        assertThrows(ClinicaException.class, () -> laboratorio.solicitar(1L, List.of("TOMOGRAFIA")));
        assertFalse(laboratorio.examenesDisponibles().isEmpty());
    }
}
