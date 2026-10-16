package com.clinica.gestion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.clinica.gestion.exception.ClinicaException;
import com.clinica.gestion.model.Medicamento;
import com.clinica.gestion.model.Prescripcion;

/**
 * SUBSISTEMA 4 - Prescripciones.
 * Genera prescripciones con medicamentos, dosis y duracion.
 */
@Service
public class PrescripcionService {

    private final Map<Long, List<Prescripcion>> prescripciones = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    public Prescripcion generar(Long pacienteId, List<Medicamento> medicamentos, String indicaciones) {
        return guardar(pacienteId, medicamentos, indicaciones, LocalDateTime.now());
    }

    /** Carga una prescripcion antigua (datos historicos de ejemplo). */
    public Prescripcion registrarHistorica(Long pacienteId, List<Medicamento> medicamentos,
                                           String indicaciones, LocalDateTime fecha) {
        return guardar(pacienteId, medicamentos, indicaciones, fecha);
    }

    public List<Prescripcion> consultarPorPaciente(Long pacienteId) {
        List<Prescripcion> lista = new ArrayList<>(prescripciones.getOrDefault(pacienteId, List.of()));
        lista.sort(Comparator.comparing(Prescripcion::fecha).reversed());
        return lista;
    }

    private Prescripcion guardar(Long pacienteId, List<Medicamento> medicamentos, String indicaciones,
                                 LocalDateTime fecha) {
        if (medicamentos == null || medicamentos.isEmpty()) {
            throw ClinicaException.invalido("La prescripcion debe tener al menos un medicamento");
        }
        for (Medicamento m : medicamentos) {
            if (m == null || vacio(m.nombre()) || vacio(m.dosis()) || vacio(m.duracion())) {
                throw ClinicaException.invalido("Cada medicamento necesita nombre, dosis y duracion");
            }
        }
        String texto = vacio(indicaciones) ? "Tomar segun las indicaciones del medico." : indicaciones.trim();
        Prescripcion prescripcion = new Prescripcion(secuencia.getAndIncrement(), pacienteId, fecha,
                List.copyOf(medicamentos), texto);
        prescripciones.computeIfAbsent(pacienteId, id -> new CopyOnWriteArrayList<>()).add(prescripcion);
        return prescripcion;
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
