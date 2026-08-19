package com.team35.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;

/**
 * Request para calcular la frecuencia de ahorro a partir de una encuesta de 5 preguntas.
 * Acepta tanto camelCase como snake_case para máxima compatibilidad.
 *
 * Endpoint: POST /api/analisis/frecuencia-ahorro-encuesta
 */
public class FrecuenciaAhorroEncuestaRequest {

    /**
     * ¿En cuántos meses (de los últimos 6) lograste ahorrar dinero?
     * Valores permitidos: 0, 1, 2, 3, 4, 5, 6
     */
    @Min(value = 0, message = "mesesAhorrados debe ser entre 0 y 6")
    @Max(value = 6, message = "mesesAhorrados debe ser entre 0 y 6")
    @JsonAlias({"mesesAhorrados", "meses_ahorrados"})
    private int mesesAhorrados;

    /**
     * Porcentaje de ingresos que suele ahorrar.
     * Valores: NO_AHORRO | MENOS_5 | 5_10 | 10_20 | MAS_20
     */
    @NotBlank
    @JsonAlias({"porcentajeAhorro", "porcentaje_ahorro"})
    private String porcentajeAhorro;

    /**
     * ¿Qué hace con el dinero que planea ahorrar cuando recibe ingresos?
     * Valores: NO_SEPARA | ESPERA_SOBRANTE | A_VECES_ANTES | CASI_SIEMPRE | SIEMPRE_PRIMERO
     */
    @NotBlank
    @JsonAlias({"separaAntesDeGastar", "separa_antes_de_gastar"})
    private String separaAntesDeGastar;

    /**
     * Si tiene un gasto inesperado, ¿qué pasa con su ahorro?
     * Valores: USA_AHORROS | NO_AHORRA | MENOR_AHORRO | MISMO_MONTO | SIN_PROBLEMA
     */
    @NotBlank
    @JsonAlias({"comportamientoImprevistos", "comportamiento_imprevistos"})
    private String comportamientoImprevistos;

    /**
     * ¿Con qué frecuencia realiza una acción específica para guardar dinero?
     * Valores: NUNCA | MENOS_MES | UNA_VEZ_MES | VARIAS_VECES_MES | UNA_VEZ_SEMANA_O_MAS
     */
    @NotBlank
    @JsonAlias({"frecuenciaAccionesAhorro", "frecuencia_acciones_ahorro"})
    private String frecuenciaAccionesAhorro;

    // Getters y Setters
    public int getMesesAhorrados() { return mesesAhorrados; }
    public void setMesesAhorrados(int mesesAhorrados) { this.mesesAhorrados = mesesAhorrados; }

    public String getPorcentajeAhorro() { return porcentajeAhorro; }
    public void setPorcentajeAhorro(String porcentajeAhorro) { this.porcentajeAhorro = porcentajeAhorro; }

    public String getSeparaAntesDeGastar() { return separaAntesDeGastar; }
    public void setSeparaAntesDeGastar(String separaAntesDeGastar) { this.separaAntesDeGastar = separaAntesDeGastar; }

    public String getComportamientoImprevistos() { return comportamientoImprevistos; }
    public void setComportamientoImprevistos(String comportamientoImprevistos) { this.comportamientoImprevistos = comportamientoImprevistos; }

    public String getFrecuenciaAccionesAhorro() { return frecuenciaAccionesAhorro; }
    public void setFrecuenciaAccionesAhorro(String frecuenciaAccionesAhorro) { this.frecuenciaAccionesAhorro = frecuenciaAccionesAhorro; }
}
