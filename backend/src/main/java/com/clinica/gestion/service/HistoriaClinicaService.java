package com.clinica.gestion.service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.clinica.gestion.exception.ClinicaException;
import com.clinica.gestion.model.Consulta;

/**
 * SUBSISTEMA 3 - Historia clinica.
 * Registra y consulta las consultas previas, diagnosticos y alergias del paciente.
 */
@Service
public class HistoriaClinicaService {

    /**
     * Medicamentos que pertenecen a la misma familia de una alergia comun.
     * Ej: alguien alergico a la penicilina tampoco puede recibir amoxicilina.
     */
    private static final Map<String, List<String>> FAMILIAS = Map.of(
            "penicilina", List.of("amoxicilina", "ampicilina", "dicloxacilina", "penicilina"),
            "aines", List.of("ibuprofeno", "naproxeno", "diclofenaco", "aspirina", "acido acetilsalicilico"),
            "sulfas", List.of("sulfametoxazol", "trimetoprim sulfametoxazol", "sulfadiazina"),
            "aspirina", List.of("aspirina", "acido acetilsalicilico"));

    private final Map<Long, Set<String>> alergias = new ConcurrentHashMap<>();
    private final Map<Long, List<Consulta>> consultas = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    /** Abre la historia clinica de un paciente nuevo con sus alergias conocidas. */
    public void crearHistoria(Long pacienteId, List<String> alergiasIniciales) {
        Set<String> conjunto = new LinkedHashSet<>();
        if (alergiasIniciales != null) {
            alergiasIniciales.stream()
                    .filter(a -> a != null && !a.isBlank())
                    .map(String::trim)
                    .forEach(conjunto::add);
        }
        alergias.put(pacienteId, conjunto);
        consultas.put(pacienteId, new CopyOnWriteArrayList<>());
    }

    public Consulta registrarConsulta(Long pacienteId, LocalDateTime fecha, String medico,
                                      String especialidad, String motivo, String diagnostico) {
        Consulta consulta = new Consulta(secuencia.getAndIncrement(), pacienteId, fecha, medico,
                especialidad, motivo, diagnostico);
        consultas.computeIfAbsent(pacienteId, id -> new CopyOnWriteArrayList<>()).add(consulta);
        return consulta;
    }

    public List<Consulta> consultarConsultas(Long pacienteId) {
        List<Consulta> lista = new ArrayList<>(consultas.getOrDefault(pacienteId, List.of()));
        lista.sort(Comparator.comparing(Consulta::fecha).reversed());
        return lista;
    }

    public List<String> consultarAlergias(Long pacienteId) {
        return List.copyOf(alergias.getOrDefault(pacienteId, Set.of()));
    }

    public void agregarAlergia(Long pacienteId, String alergia) {
        alergias.computeIfAbsent(pacienteId, id -> new LinkedHashSet<>()).add(alergia.trim());
    }

    /**
     * Revisa que ningun medicamento choque con las alergias del paciente.
     * Lanza un error 409 indicando el medicamento y la alergia en conflicto.
     */
    public void validarAlergias(Long pacienteId, List<String> medicamentos) {
        for (String alergia : consultarAlergias(pacienteId)) {
            String a = normalizar(alergia);
            List<String> prohibidos = FAMILIAS.getOrDefault(a, List.of(a));
            for (String medicamento : medicamentos) {
                String m = normalizar(medicamento);
                boolean choca = m.contains(a) || prohibidos.stream().anyMatch(m::contains);
                if (choca) {
                    throw ClinicaException.conflicto("No se puede prescribir " + medicamento
                            + ": el paciente es alergico a " + alergia);
                }
            }
        }
    }

    private String normalizar(String texto) {
        return Normalizer.normalize(texto.trim().toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
