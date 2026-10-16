package com.clinica.gestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.clinica.gestion.dto.HistoriaCompleta;
import com.clinica.gestion.dto.RegistroPacienteRequest;
import com.clinica.gestion.exception.ClinicaException;
import com.clinica.gestion.facade.ClinicaFacade;
import com.clinica.gestion.model.Cita;
import com.clinica.gestion.model.Medicamento;
import com.clinica.gestion.model.Paciente;
import com.clinica.gestion.service.AgendaService;
import com.clinica.gestion.service.HistoriaClinicaService;
import com.clinica.gestion.service.LaboratorioService;
import com.clinica.gestion.service.PacienteService;
import com.clinica.gestion.service.PrescripcionService;

/**
 * Pruebas de la fachada: el cliente solo usa ClinicaFacade.
 */
class ClinicaFacadeTest {

    private ClinicaFacade facade;
    private Paciente paciente;

    @BeforeEach
    void preparar() {
        facade = new ClinicaFacade(new PacienteService(), new AgendaService(), new HistoriaClinicaService(),
                new PrescripcionService(), new LaboratorioService());
        paciente = facade.registrarPaciente(new RegistroPacienteRequest("12345678", "Ana", "Perez",
                "ana@correo.com", null, LocalDate.of(2000, 1, 1), "secreto1", List.of("Penicilina")));
    }

    @Test
    void noPermiteDocumentoRepetido() {
        assertThrows(ClinicaException.class, () -> facade.registrarPaciente(new RegistroPacienteRequest(
                "12345678", "Otro", "Paciente", "otro@correo.com", null, null, "secreto1", null)));
    }

    @Test
    void agendaCitaConRecordatorio() {
        LocalDateTime lunes = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atTime(9, 0);
        Cita cita = facade.agendarCita(paciente.id(), "Cardiologia", lunes);

        assertEquals("Cardiología", cita.getEspecialidad());
        assertNotNull(cita.getRecordatorio());
        assertThrows(ClinicaException.class,
                () -> facade.agendarCita(paciente.id(), "Cardiologia", lunes.minusDays(30)));
    }

    @Test
    void bloqueaPrescripcionPorAlergia() {
        assertThrows(ClinicaException.class, () -> facade.generarPrescripcion(paciente.id(),
                List.of(new Medicamento("Amoxicilina 500 mg", "1 cada 8 horas", "7 dias"))));
    }

    @Test
    void historiaCompletaConsolidaTodo() {
        LocalDateTime martes = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.TUESDAY)).atTime(10, 0);
        facade.agendarCita(paciente.id(), "Dermatologia", martes);
        facade.generarPrescripcion(paciente.id(),
                List.of(new Medicamento("Loratadina 10 mg", "1 diaria", "10 dias")));
        facade.solicitarExamenes(paciente.id(), List.of("GLICEMIA"));

        HistoriaCompleta historia = facade.verHistoriaCompleta(paciente.id());
        assertEquals(1, historia.proximasCitas().size());
        assertEquals(1, historia.prescripciones().size());
        assertEquals(1, historia.laboratorios().size());
        assertFalse(historia.alergias().isEmpty());
    }

    @Test
    void consultaMedicosConHorarios() {
        LocalDate miercoles = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
        assertEquals(2, facade.consultarMedicos("Medicina General", miercoles).size());
        assertFalse(facade.consultarMedicos("Medicina General", miercoles).get(0).horariosDisponibles().isEmpty());
    }
}
