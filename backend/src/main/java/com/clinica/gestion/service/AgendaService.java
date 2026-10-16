package com.clinica.gestion.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.clinica.gestion.exception.ClinicaException;
import com.clinica.gestion.model.Cita;
import com.clinica.gestion.model.Medico;

/**
 * SUBSISTEMA 2 - Agenda.
 * Disponibilidad de medicos por especialidad, agendar y cancelar citas,
 * y recordatorio incluido en la respuesta.
 */
@Service
public class AgendaService {

    /** Horario de atencion: de 8:00 a 16:00, citas de una hora (sin la hora de almuerzo). */
    private static final List<LocalTime> HORARIOS = List.of(
            LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0));

    private static final DateTimeFormatter FORMATO_RECORDATORIO =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'a las' HH:mm", Locale.forLanguageTag("es-CO"));

    /** 3 especialidades y 5 medicos predefinidos. */
    private final List<Medico> medicos = List.of(
            new Medico(1L, "Dra. Laura Benavides", "Medicina General"),
            new Medico(2L, "Dr. Andres Rosero", "Medicina General"),
            new Medico(3L, "Dr. Carlos Erazo", "Cardiología"),
            new Medico(4L, "Dra. Paola Burbano", "Cardiología"),
            new Medico(5L, "Dra. Diana Insuasty", "Dermatología"));

    private final Map<Long, Cita> citas = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    public List<String> especialidades() {
        return medicos.stream().map(Medico::especialidad).distinct().toList();
    }

    public List<Medico> medicosPorEspecialidad(String especialidad) {
        if (especialidad == null || especialidad.isBlank()) {
            return medicos;
        }
        List<Medico> resultado = medicos.stream()
                .filter(m -> normalizar(m.especialidad()).equals(normalizar(especialidad)))
                .toList();
        if (resultado.isEmpty()) {
            throw ClinicaException.noEncontrado("No hay medicos para la especialidad " + especialidad
                    + ". Especialidades disponibles: " + especialidades());
        }
        return resultado;
    }

    public Medico buscarMedico(Long medicoId) {
        return medicos.stream().filter(m -> m.id().equals(medicoId)).findFirst()
                .orElseThrow(() -> ClinicaException.noEncontrado("No existe el medico con id " + medicoId));
    }

    /** Horarios libres de un medico en un dia (no muestra horas pasadas ni domingos). */
    public List<String> horariosDisponibles(Long medicoId, LocalDate dia) {
        if (dia == null || dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return List.of();
        }
        return HORARIOS.stream()
                .map(hora -> LocalDateTime.of(dia, hora))
                .filter(fecha -> fecha.isAfter(LocalDateTime.now()))
                .filter(fecha -> estaLibre(medicoId, fecha))
                .map(fecha -> fecha.toLocalTime().toString())
                .toList();
    }

    public Cita agendar(Long pacienteId, String especialidad, LocalDateTime fecha, Long medicoId) {
        if (fecha == null || !fecha.isAfter(LocalDateTime.now())) {
            throw ClinicaException.invalido("La cita debe ser en una fecha futura");
        }
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY || !HORARIOS.contains(fecha.toLocalTime())) {
            throw ClinicaException.invalido("Horario no valido. Horarios de lunes a sabado: " + HORARIOS);
        }
        List<Medico> candidatos = medicosPorEspecialidad(especialidad);
        Medico medico;
        if (medicoId != null) {
            medico = candidatos.stream().filter(m -> m.id().equals(medicoId)).findFirst()
                    .orElseThrow(() -> ClinicaException.invalido("El medico elegido no pertenece a " + especialidad));
            if (!estaLibre(medico.id(), fecha)) {
                throw ClinicaException.conflicto(medico.nombre() + " ya tiene una cita en ese horario");
            }
        } else {
            medico = candidatos.stream().filter(m -> estaLibre(m.id(), fecha)).findFirst()
                    .orElseThrow(() -> ClinicaException.conflicto("No hay medicos libres de " + especialidad + " en ese horario"));
        }
        boolean pacienteOcupado = citas.values().stream()
                .anyMatch(c -> c.getPacienteId().equals(pacienteId) && c.getEstado() == Cita.Estado.PROGRAMADA
                        && c.getFecha().equals(fecha));
        if (pacienteOcupado) {
            throw ClinicaException.conflicto("Usted ya tiene otra cita a esa misma hora");
        }
        Cita cita = new Cita(secuencia.getAndIncrement(), pacienteId, medico, fecha);
        cita.setRecordatorio("Recuerde su cita de " + medico.especialidad() + " con " + medico.nombre()
                + " el " + fecha.format(FORMATO_RECORDATORIO)
                + ". Llegue 15 minutos antes con su documento de identidad.");
        citas.put(cita.getId(), cita);
        return cita;
    }

    /** Carga una cita ya atendida (datos historicos de ejemplo). */
    public Cita registrarCitaAtendida(Long pacienteId, Long medicoId, LocalDateTime fecha) {
        Cita cita = new Cita(secuencia.getAndIncrement(), pacienteId, buscarMedico(medicoId), fecha);
        citas.put(cita.getId(), cita);
        return cita;
    }

    public Cita cancelar(Long citaId, Long pacienteId) {
        Cita cita = citas.get(citaId);
        if (cita == null || !cita.getPacienteId().equals(pacienteId)) {
            throw ClinicaException.noEncontrado("No existe la cita " + citaId + " para este paciente");
        }
        if (cita.esPasada()) {
            throw ClinicaException.invalido("No se puede cancelar una cita que ya paso");
        }
        cita.setEstado(Cita.Estado.CANCELADA);
        cita.setRecordatorio("Su cita fue cancelada. Puede agendar una nueva cuando lo necesite.");
        return cita;
    }

    public List<Cita> citasDePaciente(Long pacienteId) {
        List<Cita> resultado = new ArrayList<>(citas.values().stream()
                .filter(c -> c.getPacienteId().equals(pacienteId)).toList());
        resultado.sort(Comparator.comparing(Cita::getFecha));
        return resultado;
    }

    private boolean estaLibre(Long medicoId, LocalDateTime fecha) {
        return citas.values().stream().noneMatch(c -> c.getMedicoId().equals(medicoId)
                && c.getEstado() == Cita.Estado.PROGRAMADA && c.getFecha().equals(fecha));
    }

    private String normalizar(String texto) {
        return java.text.Normalizer.normalize(texto.trim().toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
