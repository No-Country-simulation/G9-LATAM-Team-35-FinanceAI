package com.team35.backend.dto;

import java.util.List;
import java.util.Map;

/**
 * perfilFinanciero se serializa como "perfil_financiero" (snake_case global)
 * y lleva el TEXTO LEGIBLE (ej. "En observación"), no el enum crudo de BD
 * (EN_OBSERVACION) — así lo pidió Data Science.
 */
public class AnalisisFinancieroResponse {

    private String perfilFinanciero;
    private double probabilidad;
    private Map<String, Double> resumenGastos;
    private List<String> recomendaciones;

    public AnalisisFinancieroResponse() {
    }

    public AnalisisFinancieroResponse(String perfilFinanciero, double probabilidad,
                                       Map<String, Double> resumenGastos, List<String> recomendaciones) {
        this.perfilFinanciero = perfilFinanciero;
        this.probabilidad = probabilidad;
        this.resumenGastos = resumenGastos;
        this.recomendaciones = recomendaciones;
    }

    public String getPerfilFinanciero() {
        return perfilFinanciero;
    }

    public void setPerfilFinanciero(String perfilFinanciero) {
        this.perfilFinanciero = perfilFinanciero;
    }

    public double getProbabilidad() {
        return probabilidad;
    }

    public void setProbabilidad(double probabilidad) {
        this.probabilidad = probabilidad;
    }

    public Map<String, Double> getResumenGastos() {
        return resumenGastos;
    }

    public void setResumenGastos(Map<String, Double> resumenGastos) {
        this.resumenGastos = resumenGastos;
    }

    public List<String> getRecomendaciones() {
        return recomendaciones;
    }

    public void setRecomendaciones(List<String> recomendaciones) {
        this.recomendaciones = recomendaciones;
    }
}
