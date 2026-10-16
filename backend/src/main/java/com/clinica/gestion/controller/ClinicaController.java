package com.clinica.gestion.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.clinica.gestion.dto.CitaRequest;
import com.clinica.gestion.dto.HistoriaCompleta;
import com.clinica.gestion.dto.LaboratorioRequest;
import com.clinica.gestion.dto.LoginRequest;
import com.clinica.gestion.dto.MedicoDisponible;
import com.clinica.gestion.dto.PrescripcionRequest;
import com.clinica.gestion.dto.RegistroPacienteRequest;
import com.clinica.gestion.facade.ClinicaFacade;
import com.clinica.gestion.model.Cita;
import com.clinica.gestion.model.Paciente;
import com.clinica.gestion.model.Prescripcion;
import com.clinica.gestion.model.ResultadoLaboratorio;

import jakarta.validation.Valid;

/**
 * CLIENTE de la fachada. El controlador solo conoce a ClinicaFacade;
 * nunca llama a los servicios directamente.
 */
@RestController
@RequestMapping("/api/clinica")
public class ClinicaController {

    private final ClinicaFacade clinicaFacade;

    public ClinicaController(ClinicaFacade clinicaFacade) {
        this.clinicaFacade = clinicaFacade;
    }

    // ---------- Endpoints pedidos en el taller ----------

    @PostMapping("/paciente")
    @ResponseStatus(HttpStatus.CREATED)
    public Paciente registrarPaciente(@Valid @RequestBody RegistroPacienteRequest request) {
        return clinicaFacade.registrarPaciente(request);
    }

    @PostMapping("/cita")
    @ResponseStatus(HttpStatus.CREATED)
    public Cita agendarCita(@Valid @RequestBody CitaRequest request) {
        return clinicaFacade.agendarCita(request.pacienteId(), request.especialidad(), request.fecha(),
                request.medicoId());
    }

    @GetMapping("/historia/{pacienteId}")
    public HistoriaCompleta verHistoria(@PathVariable Long pacienteId) {
        return clinicaFacade.verHistoriaCompleta(pacienteId);
    }

    @PostMapping("/prescripcion")
    @ResponseStatus(HttpStatus.CREATED)
    public Prescripcion generarPrescripcion(@Valid @RequestBody PrescripcionRequest request) {
        return clinicaFacade.generarPrescripcion(request.pacienteId(), request.medicamentos(),
                request.indicaciones());
    }

    @PostMapping("/laboratorio")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResultadoLaboratorio> solicitarExamenes(@Valid @RequestBody LaboratorioRequest request) {
        return clinicaFacade.solicitarExamenes(request.pacienteId(), request.examenes());
    }

    /** GET /api/clinica/medicos?especialidad=Cardiologia&fecha=2026-10-20 (fecha opcional) */
    @GetMapping("/medicos")
    public List<MedicoDisponible> consultarMedicos(
            @RequestParam(required = false) String especialidad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return clinicaFacade.consultarMedicos(especialidad, fecha);
    }

    // ---------- Endpoints de apoyo para el frontend ----------

    @PostMapping("/login")
    public Paciente iniciarSesion(@Valid @RequestBody LoginRequest request) {
        return clinicaFacade.iniciarSesion(request.documento(), request.password());
    }

    @GetMapping("/paciente/{pacienteId}")
    public Paciente consultarPerfil(@PathVariable Long pacienteId) {
        return clinicaFacade.consultarPerfil(pacienteId);
    }

    @DeleteMapping("/cita/{citaId}")
    public Cita cancelarCita(@PathVariable Long citaId, @RequestParam Long pacienteId) {
        return clinicaFacade.cancelarCita(pacienteId, citaId);
    }

    @GetMapping("/especialidades")
    public List<String> consultarEspecialidades() {
        return clinicaFacade.consultarEspecialidades();
    }

    @GetMapping("/laboratorio/examenes")
    public Map<String, String> consultarExamenes() {
        return clinicaFacade.consultarExamenesDisponibles();
    }
}
