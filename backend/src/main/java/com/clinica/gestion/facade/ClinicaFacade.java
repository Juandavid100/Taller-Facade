package com.clinica.gestion.facade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.clinica.gestion.dto.HistoriaCompleta;
import com.clinica.gestion.dto.MedicoDisponible;
import com.clinica.gestion.dto.RegistroPacienteRequest;
import com.clinica.gestion.model.Cita;
import com.clinica.gestion.model.Medicamento;
import com.clinica.gestion.model.Paciente;
import com.clinica.gestion.model.Prescripcion;
import com.clinica.gestion.model.ResultadoLaboratorio;
import com.clinica.gestion.service.AgendaService;
import com.clinica.gestion.service.HistoriaClinicaService;
import com.clinica.gestion.service.LaboratorioService;
import com.clinica.gestion.service.PacienteService;
import com.clinica.gestion.service.PrescripcionService;

/**
 * PATRON FACADE.
 *
 * Es el UNICO punto de entrada que usa el controlador (el cliente).
 * Oculta los 5 subsistemas y coordina las validaciones entre ellos:
 *
 *   Controlador --> ClinicaFacade --> PacienteService
 *                                 --> AgendaService
 *                                 --> HistoriaClinicaService
 *                                 --> PrescripcionService
 *                                 --> LaboratorioService
 *
 * Los subsistemas no se conocen entre si; solo la fachada los combina.
 * Cada metodo principal invoca minimo 2 subsistemas.
 */
@Component
public class ClinicaFacade {

    private final PacienteService pacienteService;
    private final AgendaService agendaService;
    private final HistoriaClinicaService historiaClinicaService;
    private final PrescripcionService prescripcionService;
    private final LaboratorioService laboratorioService;

    public ClinicaFacade(PacienteService pacienteService,
                         AgendaService agendaService,
                         HistoriaClinicaService historiaClinicaService,
                         PrescripcionService prescripcionService,
                         LaboratorioService laboratorioService) {
        this.pacienteService = pacienteService;
        this.agendaService = agendaService;
        this.historiaClinicaService = historiaClinicaService;
        this.prescripcionService = prescripcionService;
        this.laboratorioService = laboratorioService;
    }

    /** Registro: PacienteService (datos + documento unico) + HistoriaClinicaService (abre historia con alergias). */
    public Paciente registrarPaciente(RegistroPacienteRequest datos) {
        Paciente paciente = pacienteService.registrar(datos.documento(), datos.nombres(), datos.apellidos(),
                datos.email(), datos.telefono(), datos.fechaNacimiento(), datos.password());
        historiaClinicaService.crearHistoria(paciente.id(), datos.alergias());
        return paciente;
    }

    /** Inicio de sesion: PacienteService. */
    public Paciente iniciarSesion(String documento, String password) {
        return pacienteService.autenticar(documento, password);
    }

    /** Perfil del paciente: PacienteService. */
    public Paciente consultarPerfil(Long pacienteId) {
        return pacienteService.consultarPerfil(pacienteId);
    }

    /** Especialidades disponibles: AgendaService. */
    public List<String> consultarEspecialidades() {
        return agendaService.especialidades();
    }

    /** Medicos de una especialidad con sus horarios libres en un dia: AgendaService. */
    public List<MedicoDisponible> consultarMedicos(String especialidad, LocalDate dia) {
        return agendaService.medicosPorEspecialidad(especialidad).stream()
                .map(m -> new MedicoDisponible(m.id(), m.nombre(), m.especialidad(),
                        dia == null ? List.of() : agendaService.horariosDisponibles(m.id(), dia)))
                .toList();
    }

    /** Firma pedida en el taller: agendarCita(pacienteId, especialidad, fecha). */
    public Cita agendarCita(Long pacienteId, String especialidad, LocalDateTime fecha) {
        return agendarCita(pacienteId, especialidad, fecha, null);
    }

    /** Agendar: PacienteService (el paciente existe) + AgendaService (disponibilidad, cita y recordatorio). */
    public Cita agendarCita(Long pacienteId, String especialidad, LocalDateTime fecha, Long medicoId) {
        pacienteService.consultarPerfil(pacienteId);
        return agendaService.agendar(pacienteId, especialidad, fecha, medicoId);
    }

    /** Cancelar: PacienteService + AgendaService. */
    public Cita cancelarCita(Long pacienteId, Long citaId) {
        pacienteService.consultarPerfil(pacienteId);
        return agendaService.cancelar(citaId, pacienteId);
    }

    /**
     * Historia completa: consolida los 5 subsistemas en un solo objeto de respuesta
     * (citas pasadas y proximas + consultas + alergias + prescripciones + laboratorios).
     */
    public HistoriaCompleta verHistoriaCompleta(Long pacienteId) {
        Paciente paciente = pacienteService.consultarPerfil(pacienteId);
        List<Cita> citas = agendaService.citasDePaciente(pacienteId);
        List<Cita> proximas = citas.stream()
                .filter(c -> !c.esPasada() && c.getEstado() == Cita.Estado.PROGRAMADA)
                .toList();
        List<Cita> pasadas = citas.stream()
                .filter(c -> c.esPasada() || c.getEstado() == Cita.Estado.CANCELADA)
                .sorted((a, b) -> b.getFecha().compareTo(a.getFecha()))
                .toList();
        return new HistoriaCompleta(
                paciente,
                historiaClinicaService.consultarAlergias(pacienteId),
                proximas,
                pasadas,
                historiaClinicaService.consultarConsultas(pacienteId),
                prescripcionService.consultarPorPaciente(pacienteId),
                laboratorioService.consultarPorPaciente(pacienteId));
    }

    /** Firma pedida en el taller: generarPrescripcion(pacienteId, medicamentos[]). */
    public Prescripcion generarPrescripcion(Long pacienteId, List<Medicamento> medicamentos) {
        return generarPrescripcion(pacienteId, medicamentos, null);
    }

    /**
     * Prescripcion: PacienteService + HistoriaClinicaService (validacion de alergias)
     * + PrescripcionService (genera la formula).
     */
    public Prescripcion generarPrescripcion(Long pacienteId, List<Medicamento> medicamentos, String indicaciones) {
        pacienteService.consultarPerfil(pacienteId);
        historiaClinicaService.validarAlergias(pacienteId,
                medicamentos.stream().map(Medicamento::nombre).toList());
        return prescripcionService.generar(pacienteId, medicamentos, indicaciones);
    }

    /** Catalogo de examenes que se pueden pedir: LaboratorioService. */
    public java.util.Map<String, String> consultarExamenesDisponibles() {
        return laboratorioService.examenesDisponibles();
    }

    /** Laboratorio: PacienteService + LaboratorioService (resultados simulados con referencias). */
    public List<ResultadoLaboratorio> solicitarExamenes(Long pacienteId, List<String> examenes) {
        pacienteService.consultarPerfil(pacienteId);
        return laboratorioService.solicitar(pacienteId, examenes);
    }
}
