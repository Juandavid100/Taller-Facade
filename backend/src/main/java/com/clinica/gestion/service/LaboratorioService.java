package com.clinica.gestion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.clinica.gestion.exception.ClinicaException;
import com.clinica.gestion.model.ParametroLaboratorio;
import com.clinica.gestion.model.ResultadoLaboratorio;

/**
 * SUBSISTEMA 5 - Laboratorio.
 * Solicita examenes (hemograma, glicemia, perfil lipidico) y devuelve
 * resultados simulados con sus valores de referencia.
 */
@Service
public class LaboratorioService {

    /** Rango de referencia de cada parametro: nombre, unidad, minimo, maximo. */
    private record Referencia(String nombre, String unidad, double minimo, double maximo) { }

    private static final Map<String, String> NOMBRES = new LinkedHashMap<>();
    private static final Map<String, List<Referencia>> REFERENCIAS = new LinkedHashMap<>();

    static {
        NOMBRES.put("HEMOGRAMA", "Hemograma");
        NOMBRES.put("GLICEMIA", "Glicemia en ayunas");
        NOMBRES.put("PERFIL_LIPIDICO", "Perfil lipídico");

        REFERENCIAS.put("HEMOGRAMA", List.of(
                new Referencia("Hemoglobina", "g/dL", 12.0, 17.5),
                new Referencia("Hematocrito", "%", 36.0, 52.0),
                new Referencia("Leucocitos", "×10³/µL", 4.5, 11.0),
                new Referencia("Plaquetas", "×10³/µL", 150.0, 450.0)));
        REFERENCIAS.put("GLICEMIA", List.of(
                new Referencia("Glucosa", "mg/dL", 70.0, 100.0)));
        REFERENCIAS.put("PERFIL_LIPIDICO", List.of(
                new Referencia("Colesterol total", "mg/dL", 0.0, 200.0),
                new Referencia("Colesterol HDL", "mg/dL", 40.0, 100.0),
                new Referencia("Colesterol LDL", "mg/dL", 0.0, 130.0),
                new Referencia("Triglicéridos", "mg/dL", 0.0, 150.0)));
    }

    private final Map<Long, List<ResultadoLaboratorio>> resultados = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    public Map<String, String> examenesDisponibles() {
        return NOMBRES;
    }

    /** Valida los codigos de examen y devuelve la lista normalizada (sin repetidos). */
    public List<String> validarExamenes(List<String> examenes) {
        if (examenes == null || examenes.isEmpty()) {
            throw ClinicaException.invalido("Debe solicitar al menos un examen");
        }
        List<String> codigos = examenes.stream()
                .map(e -> e.trim().toUpperCase(Locale.ROOT).replace(' ', '_'))
                .distinct()
                .toList();
        for (String codigo : codigos) {
            if (!REFERENCIAS.containsKey(codigo)) {
                throw ClinicaException.invalido("Examen no disponible: " + codigo
                        + ". Disponibles: " + NOMBRES.keySet());
            }
        }
        return codigos;
    }

    public List<ResultadoLaboratorio> solicitar(Long pacienteId, List<String> examenes) {
        List<ResultadoLaboratorio> nuevos = new ArrayList<>();
        for (String codigo : validarExamenes(examenes)) {
            nuevos.add(guardar(pacienteId, codigo, simular(codigo), LocalDateTime.now()));
        }
        return nuevos;
    }

    /** Carga un resultado antiguo con valores fijos (datos historicos de ejemplo). */
    public ResultadoLaboratorio registrarHistorico(Long pacienteId, String codigo, double[] valores,
                                                   LocalDateTime fecha) {
        List<Referencia> refs = REFERENCIAS.get(codigo);
        List<ParametroLaboratorio> parametros = new ArrayList<>();
        for (int i = 0; i < refs.size(); i++) {
            Referencia r = refs.get(i);
            parametros.add(ParametroLaboratorio.de(r.nombre(), valores[i], r.unidad(), r.minimo(), r.maximo()));
        }
        return guardar(pacienteId, codigo, parametros, fecha);
    }

    public List<ResultadoLaboratorio> consultarPorPaciente(Long pacienteId) {
        List<ResultadoLaboratorio> lista = new ArrayList<>(resultados.getOrDefault(pacienteId, List.of()));
        lista.sort(Comparator.comparing(ResultadoLaboratorio::fecha).reversed());
        return lista;
    }

    private ResultadoLaboratorio guardar(Long pacienteId, String codigo, List<ParametroLaboratorio> parametros,
                                         LocalDateTime fecha) {
        ResultadoLaboratorio resultado = new ResultadoLaboratorio(secuencia.getAndIncrement(), pacienteId,
                codigo, NOMBRES.get(codigo), fecha, parametros);
        resultados.computeIfAbsent(pacienteId, id -> new CopyOnWriteArrayList<>()).add(resultado);
        return resultado;
    }

    /** Genera valores aleatorios: la mayoria normales y algunos fuera de rango. */
    private List<ParametroLaboratorio> simular(String codigo) {
        ThreadLocalRandom azar = ThreadLocalRandom.current();
        List<ParametroLaboratorio> parametros = new ArrayList<>();
        for (Referencia r : REFERENCIAS.get(codigo)) {
            double ancho = r.maximo() - r.minimo();
            double valor;
            if (azar.nextDouble() < 0.75) {
                valor = r.minimo() + ancho * azar.nextDouble(0.1, 0.9);
            } else if (azar.nextBoolean() && r.minimo() > 0) {
                valor = r.minimo() * azar.nextDouble(0.75, 0.97);
            } else {
                valor = r.maximo() * azar.nextDouble(1.03, 1.3);
            }
            valor = Math.round(valor * 10) / 10.0;
            parametros.add(ParametroLaboratorio.de(r.nombre(), valor, r.unidad(), r.minimo(), r.maximo()));
        }
        return parametros;
    }
}
