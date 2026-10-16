package com.clinica.gestion.model;

/**
 * Valor medido en un examen con su rango de referencia.
 * dentroDeRango le dice al frontend si debe pintarlo en verde o en rojo.
 */
public record ParametroLaboratorio(
        String nombre,
        double valor,
        String unidad,
        double minimo,
        double maximo,
        boolean dentroDeRango) {

    public static ParametroLaboratorio de(String nombre, double valor, String unidad, double minimo, double maximo) {
        return new ParametroLaboratorio(nombre, valor, unidad, minimo, maximo, valor >= minimo && valor <= maximo);
    }
}
