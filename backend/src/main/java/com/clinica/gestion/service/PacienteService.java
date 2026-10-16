package com.clinica.gestion.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.clinica.gestion.exception.ClinicaException;
import com.clinica.gestion.model.Paciente;

/**
 * SUBSISTEMA 1 - Pacientes.
 * Registro con datos personales, validacion de documento unico y consulta de perfil.
 * Los datos se guardan en memoria.
 */
@Service
public class PacienteService {

    private final Map<Long, Paciente> pacientes = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    public Paciente registrar(String documento, String nombres, String apellidos, String email,
                              String telefono, LocalDate fechaNacimiento, String password) {
        validarDocumentoUnico(documento);
        if (fechaNacimiento != null && fechaNacimiento.isAfter(LocalDate.now())) {
            throw ClinicaException.invalido("La fecha de nacimiento no puede ser futura");
        }
        Paciente paciente = new Paciente(secuencia.getAndIncrement(), documento.trim(), nombres.trim(),
                apellidos.trim(), email.trim().toLowerCase(), telefono, fechaNacimiento, hash(password));
        pacientes.put(paciente.id(), paciente);
        return paciente;
    }

    public void validarDocumentoUnico(String documento) {
        if (documento == null || !documento.trim().matches("\\d{6,12}")) {
            throw ClinicaException.invalido("El documento debe tener entre 6 y 12 digitos");
        }
        boolean existe = pacientes.values().stream()
                .anyMatch(p -> p.documento().equals(documento.trim()));
        if (existe) {
            throw ClinicaException.conflicto("Ya existe un paciente registrado con el documento " + documento);
        }
    }

    public Paciente autenticar(String documento, String password) {
        return pacientes.values().stream()
                .filter(p -> p.documento().equals(documento.trim()))
                .filter(p -> p.passwordHash().equals(hash(password)))
                .findFirst()
                .orElseThrow(() -> new ClinicaException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Documento o contrasena incorrectos"));
    }

    public Paciente consultarPerfil(Long pacienteId) {
        Paciente paciente = pacientes.get(pacienteId);
        if (paciente == null) {
            throw ClinicaException.noEncontrado("No existe el paciente con id " + pacienteId);
        }
        return paciente;
    }

    private String hash(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
