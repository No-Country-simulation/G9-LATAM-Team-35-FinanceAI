package com.team35.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.team35.backend.enums.PerfilTipo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AnalisisDetails {

    private Long id;
    private PerfilTipo perfil;
    private BigDecimal probabilidad;

    @JsonAlias({"ingresoMensual", "ingreso_mensual"})
    private BigDecimal ingresoMensual;

    @JsonAlias({"nivelEndeudamiento", "nivel_endeudamiento"})
    private BigDecimal nivelEndeudamiento;

    @JsonAlias({"frecuenciaAhorro", "frecuencia_ahorro"})
    private String frecuenciaAhorro;

    @JsonAlias({"fechaAnalisis", "fecha_analisis"})
    private LocalDateTime fechaAnalisis;

    private String nombre;

    public AnalisisDetails() {
    }

    public AnalisisDetails(
            Long id,
            PerfilTipo perfil,
            BigDecimal probabilidad,
            BigDecimal ingresoMensual,
            BigDecimal nivelEndeudamiento,
            String frecuenciaAhorro,
            LocalDateTime fechaAnalisis
    ) {
        this.id = id;
        this.perfil = perfil;
        this.probabilidad = probabilidad;
        this.ingresoMensual = ingresoMensual;
        this.nivelEndeudamiento = nivelEndeudamiento;
        this.frecuenciaAhorro = frecuenciaAhorro;
        this.fechaAnalisis = fechaAnalisis;
        this.nombre = generarNombre(fechaAnalisis, perfil != null ? perfil.name() : "SALUDABLE");
    }

    public AnalisisDetails(
            Long id,
            PerfilTipo perfil,
            BigDecimal probabilidad,
            BigDecimal ingresoMensual,
            BigDecimal nivelEndeudamiento,
            String frecuenciaAhorro,
            LocalDateTime fechaAnalisis,
            String nombre
    ) {
        this.id = id;
        this.perfil = perfil;
        this.probabilidad = probabilidad;
        this.ingresoMensual = ingresoMensual;
        this.nivelEndeudamiento = nivelEndeudamiento;
        this.frecuenciaAhorro = frecuenciaAhorro;
        this.fechaAnalisis = fechaAnalisis;
        this.nombre = (nombre != null && !nombre.isBlank()) ? nombre : generarNombre(fechaAnalisis, perfil != null ? perfil.name() : "SALUDABLE");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PerfilTipo getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilTipo perfil) {
        this.perfil = perfil;
    }

    public BigDecimal getProbabilidad() {
        return probabilidad;
    }

    public void setProbabilidad(BigDecimal probabilidad) {
        this.probabilidad = probabilidad;
    }

    public BigDecimal getIngresoMensual() {
        return ingresoMensual;
    }

    public void setIngresoMensual(BigDecimal ingresoMensual) {
        this.ingresoMensual = ingresoMensual;
    }

    public BigDecimal getNivelEndeudamiento() {
        return nivelEndeudamiento;
    }

    public void setNivelEndeudamiento(BigDecimal nivelEndeudamiento) {
        this.nivelEndeudamiento = nivelEndeudamiento;
    }

    public String getFrecuenciaAhorro() {
        return frecuenciaAhorro;
    }

    public void setFrecuenciaAhorro(String frecuenciaAhorro) {
        this.frecuenciaAhorro = frecuenciaAhorro;
    }

    public LocalDateTime getFechaAnalisis() {
        return fechaAnalisis;
    }

    public void setFechaAnalisis(LocalDateTime fechaAnalisis) {
        this.fechaAnalisis = fechaAnalisis;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Genera el nombre en formato: "Mes Año - PERFIL"
     * Ejemplo: "Agosto 2026 - SALUDABLE"
     */
    private String generarNombre(LocalDateTime fecha, String perfil) {
        if (fecha == null) return "Análisis Financiero";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        String mesAnio = fecha.format(formatter);
        // Capitalizar primera letra del mes (en español)
        mesAnio = mesAnio.substring(0, 1).toUpperCase() + mesAnio.substring(1);
        return mesAnio + " - " + perfil;
    }
}