package com.clinica.gestion.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.clinica.gestion.dto.RegistroPacienteRequest;
import com.clinica.gestion.facade.ClinicaFacade;
import com.clinica.gestion.model.Medicamento;
import com.clinica.gestion.model.Paciente;
import com.clinica.gestion.service.AgendaService;
import com.clinica.gestion.service.HistoriaClinicaService;
import com.clinica.gestion.service.LaboratorioService;
import com.clinica.gestion.service.PrescripcionService;

/**
 * Carga un paciente de demostracion al arrancar (los datos viven en memoria,
 * asi que se recrean cada vez que Railway reinicia el servicio).
 *
 * Usuario demo: documento 1085000000 / contrasena demo123
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final ClinicaFacade clinicaFacade;
    private final AgendaService agendaService;
    private final HistoriaClinicaService historiaClinicaService;
    private final PrescripcionService prescripcionService;
    private final LaboratorioService laboratorioService;

    public DatosIniciales(ClinicaFacade clinicaFacade, AgendaService agendaService,
                          HistoriaClinicaService historiaClinicaService,
                          PrescripcionService prescripcionService, LaboratorioService laboratorioService) {
        this.clinicaFacade = clinicaFacade;
        this.agendaService = agendaService;
        this.historiaClinicaService = historiaClinicaService;
        this.prescripcionService = prescripcionService;
        this.laboratorioService = laboratorioService;
    }

    @Override
    public void run(String... args) {
        Paciente demo = clinicaFacade.registrarPaciente(new RegistroPacienteRequest(
                "1085000000", "Maria Camila", "Lopez Ortega", "demo@clinica.com", "3001234567",
                LocalDate.of(1998, 5, 14), "demo123", List.of("Penicilina")));

        LocalDateTime haceUnMes = LocalDateTime.now().minusMonths(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime haceDosSemanas = LocalDateTime.now().minusWeeks(2).withHour(10).withMinute(0).withSecond(0).withNano(0);

        agendaService.registrarCitaAtendida(demo.id(), 1L, haceUnMes);
        historiaClinicaService.registrarConsulta(demo.id(), haceUnMes, "Dra. Laura Benavides",
                "Medicina General", "Dolor de garganta y fiebre", "Faringitis aguda");
        prescripcionService.registrarHistorica(demo.id(), List.of(
                new Medicamento("Azitromicina 500 mg", "1 tableta cada 24 horas", "3 dias"),
                new Medicamento("Acetaminofen 500 mg", "1 tableta cada 8 horas si hay fiebre", "5 dias")),
                "Abundante liquido y reposo.", haceUnMes);

        agendaService.registrarCitaAtendida(demo.id(), 3L, haceDosSemanas);
        historiaClinicaService.registrarConsulta(demo.id(), haceDosSemanas, "Dr. Carlos Erazo",
                "Cardiología", "Control de colesterol", "Dislipidemia leve en seguimiento");
        laboratorioService.registrarHistorico(demo.id(), "PERFIL_LIPIDICO",
                new double[]{224, 46, 148, 132}, haceDosSemanas);
        laboratorioService.registrarHistorico(demo.id(), "GLICEMIA", new double[]{92}, haceDosSemanas);

        // Una cita futura para que el dashboard muestre "proximas citas" desde el inicio.
        LocalDate dia = LocalDate.now().plusDays(3);
        if (dia.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            dia = dia.plusDays(1);
        }
        clinicaFacade.agendarCita(demo.id(), "Cardiología", dia.atTime(10, 0), 3L);
    }
}
