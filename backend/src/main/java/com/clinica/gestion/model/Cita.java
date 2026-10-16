package com.clinica.gestion.model;

import java.time.LocalDateTime;

/**
 * Cita medica. Puede pasar de PROGRAMADA a CANCELADA.
 */
public class Cita {

    public enum Estado { PROGRAMADA, CANCELADA }

    private final Long id;
    private final Long pacienteId;
    private final Long medicoId;
    private final String medicoNombre;
    private final String especialidad;
    private final LocalDateTime fecha;
    private Estado estado;
    private String recordatorio;

    public Cita(Long id, Long pacienteId, Medico medico, LocalDateTime fecha) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medico.id();
        this.medicoNombre = medico.nombre();
        this.especialidad = medico.especialidad();
        this.fecha = fecha;
        this.estado = Estado.PROGRAMADA;
    }

    public Long getId() { return id; }
    public Long getPacienteId() { return pacienteId; }
    public Long getMedicoId() { return medicoId; }
    public String getMedicoNombre() { return medicoNombre; }
    public String getEspecialidad() { return especialidad; }
    public LocalDateTime getFecha() { return fecha; }
    public Estado getEstado() { return estado; }
    public String getRecordatorio() { return recordatorio; }

    public void setEstado(Estado estado) { this.estado = estado; }
    public void setRecordatorio(String recordatorio) { this.recordatorio = recordatorio; }

    public boolean esPasada() {
        return fecha.isBefore(LocalDateTime.now());
    }
}
