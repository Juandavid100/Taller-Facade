package com.clinica.gestion.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pagina de inicio de la API: sirve para comprobar que el despliegue en Railway esta vivo.
 */
@RestController
public class InicioController {

    @GetMapping({"/", "/api/clinica/health"})
    public Map<String, Object> inicio() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("proyecto", "Sistema de Gestion Medica - Taller Patron Facade");
        info.put("estado", "ok");
        info.put("integrantes", List.of("Juan David Moreno", "David Campiño", "Andres Delgado"));
        info.put("endpoints", List.of(
                "POST /api/clinica/paciente",
                "POST /api/clinica/cita",
                "GET /api/clinica/historia/{pacienteId}",
                "POST /api/clinica/prescripcion",
                "POST /api/clinica/laboratorio",
                "GET /api/clinica/medicos?especialidad="));
        return info;
    }
}
