package com.team35.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * Contrato de análisis financiero. Acepta tanto camelCase como snake_case.
 */
public class AnalisisFinancieroRequest {

    @Positive(message = "El ingreso mensual debe ser mayor que cero")
    @NotNull(message = "El ingreso mensual es obligatorio")
    @JsonProperty("ingreso_mensual")
    private double ingresoMensual;

    @Min(value = 0, message = "El nivel de endeudamiento no puede ser negativo")
    @Max(value = 100, message = "El nivel de endeudamiento no puede ser mayor a 100")
    @JsonProperty("nivel_endeudamiento")
    private double nivelEndeudamiento;

    @NotBlank(message = "La frecuencia de ahorro es obligatoria")
    @Pattern(regexp = "(?i)Baja|Media|Alta", message = "frecuencia_ahorro debe ser Baja, Media o Alta")
    @JsonProperty("frecuencia_ahorro")
    private String frecuenciaAhorro;

    @NotEmpty(message = "Debe incluir al menos una transacción")
    @Valid
    private List<TransaccionInputDTO> transacciones;

    public double getIngresoMensual() {
        return ingresoMensual;
    }

    public void setIngresoMensual(double ingresoMensual) {
        this.ingresoMensual = ingresoMensual;
    }

    public double getNivelEndeudamiento() {
        return nivelEndeudamiento;
    }

    public void setNivelEndeudamiento(double nivelEndeudamiento) {
        this.nivelEndeudamiento = nivelEndeudamiento;
    }

    public String getFrecuenciaAhorro() {
        return frecuenciaAhorro;
    }

    public void setFrecuenciaAhorro(String frecuenciaAhorro) {
        this.frecuenciaAhorro = frecuenciaAhorro;
    }

    public List<TransaccionInputDTO> getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(List<TransaccionInputDTO> transacciones) {
        this.transacciones = transacciones;
    }
}
