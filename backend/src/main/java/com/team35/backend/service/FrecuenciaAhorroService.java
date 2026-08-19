package com.team35.backend.service;

import com.team35.backend.dto.FrecuenciaAhorroEncuestaRequest;
import com.team35.backend.dto.FrecuenciaAhorroResponse;
import com.team35.backend.enums.TipoTransaccion;
import com.team35.backend.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Servicio híbrido para calcular la frecuencia de ahorro:
 * - Si el usuario tiene >= 3 meses de transacciones históricas: cálculo automático.
 * - Si no: usa la respuesta de la encuesta guardada.
 *
 * También expone el método de encuesta directamente (para el onboarding y la vista de análisis).
 */
@Service
@RequiredArgsConstructor
public class FrecuenciaAhorroService {

    private final TransaccionRepository transaccionRepository;

    // ─────────────────────────────────────────
    //  MÉTODO 1: Cálculo automático desde BD
    // ─────────────────────────────────────────

    /**
     * Calcula la frecuencia de ahorro analizando hasta los últimos 6 meses de transacciones del usuario.
     * Fórmula: para cada mes, si (ingresos - gastos) > 0 → mes con ahorro.
     * Clasificación:
     *   0-2 meses con ahorro → BAJA
     *   3-4 meses con ahorro → MEDIA
     *   5-6 meses con ahorro → ALTA
     *
     * @param usuarioId ID del usuario
     * @return respuesta con frecuencia, meses con datos y método (AUTOMATICO)
     */
    public FrecuenciaAhorroResponse calcularDesdeHistorial(Long usuarioId) {
        LocalDate hoy = LocalDate.now();
        int mesesConDatos = 0;
        int mesesConAhorro = 0;

        // Analizar hasta los últimos 6 meses (mes actual incluido)
        for (int i = 0; i < 6; i++) {
            YearMonth ym = YearMonth.from(hoy).minusMonths(i);
            LocalDate inicio = ym.atDay(1);
            LocalDate fin = ym.atEndOfMonth();

            BigDecimal ingresos = transaccionRepository.calcularTotalPorTipoYPeriodo(
                    usuarioId, TipoTransaccion.INGRESO, inicio, fin);
            BigDecimal gastos = transaccionRepository.calcularTotalPorTipoYPeriodo(
                    usuarioId, TipoTransaccion.GASTO, inicio, fin);

            // Solo contamos el mes si tiene al menos algún movimiento registrado
            boolean tieneMovimientos = ingresos.compareTo(BigDecimal.ZERO) > 0
                    || gastos.compareTo(BigDecimal.ZERO) > 0;

            if (tieneMovimientos) {
                mesesConDatos++;
                if (ingresos.subtract(gastos).compareTo(BigDecimal.ZERO) > 0) {
                    mesesConAhorro++;
                }
            }
        }

        String frecuencia = clasificar(mesesConAhorro);
        return new FrecuenciaAhorroResponse("AUTOMATICO", frecuencia, mesesConDatos);
    }

    /**
     * Indica si el usuario tiene suficiente historial para el cálculo automático (mínimo 3 meses con datos).
     */
    public boolean tieneSuficienteHistorial(Long usuarioId) {
        LocalDate hoy = LocalDate.now();
        int mesesConDatos = 0;
        for (int i = 0; i < 6; i++) {
            YearMonth ym = YearMonth.from(hoy).minusMonths(i);
            LocalDate inicio = ym.atDay(1);
            LocalDate fin = ym.atEndOfMonth();

            BigDecimal ingresos = transaccionRepository.calcularTotalPorTipoYPeriodo(
                    usuarioId, TipoTransaccion.INGRESO, inicio, fin);
            BigDecimal gastos = transaccionRepository.calcularTotalPorTipoYPeriodo(
                    usuarioId, TipoTransaccion.GASTO, inicio, fin);

            if (ingresos.compareTo(BigDecimal.ZERO) > 0 || gastos.compareTo(BigDecimal.ZERO) > 0) {
                mesesConDatos++;
            }
        }
        return mesesConDatos >= 3;
    }

    // ─────────────────────────────────────────
    //  MÉTODO 2: Cálculo por encuesta
    // ─────────────────────────────────────────

    /**
     * Calcula la frecuencia de ahorro a partir de las respuestas de la encuesta de 5 preguntas.
     * Cada pregunta tiene un peso de 1 punto máximo, total 5 puntos → normalizado a escala 4.
     *
     * Preguntas:
     *  1. mesesAhorrados (0-6)
     *  2. porcentajeAhorro (NO_AHORRO | MENOS_5 | 5_10 | 10_20 | MAS_20)
     *  3. separaAntesDeGastar (NO_SEPARA | ESPERA_SOBRANTE | A_VECES_ANTES | CASI_SIEMPRE | SIEMPRE_PRIMERO)
     *  4. comportamientoImprevistos (USA_AHORROS | NO_AHORRA | MENOR_AHORRO | MISMO_MONTO | SIN_PROBLEMA)
     *  5. frecuenciaAccionesAhorro (NUNCA | MENOS_MES | UNA_VEZ_MES | VARIAS_VECES_MES | UNA_VEZ_SEMANA_O_MAS)
     *
     * Clasificación del puntaje final (sobre 4):
     *   < 1.5  → BAJA
     *   1.5-2.9 → MEDIA
     *   >= 3.0 → ALTA
     */
    public FrecuenciaAhorroResponse calcularDesdeEncuesta(FrecuenciaAhorroEncuestaRequest req) {
        double puntos = 0.0;

        // Pregunta 1: mesesAhorrados (0-6) → normalizado a 0-1
        puntos += Math.min(req.getMesesAhorrados() / 6.0, 1.0);

        // Pregunta 2: porcentajeAhorro
        puntos += switch (req.getPorcentajeAhorro().toUpperCase()) {
            case "NO_AHORRO"  -> 0.0;
            case "MENOS_5"    -> 0.25;
            case "5_10"       -> 0.50;
            case "10_20"      -> 0.75;
            case "MAS_20"     -> 1.0;
            default           -> 0.0;
        };

        // Pregunta 3: separaAntesDeGastar
        puntos += switch (req.getSeparaAntesDeGastar().toUpperCase()) {
            case "NO_SEPARA"       -> 0.0;
            case "ESPERA_SOBRANTE" -> 0.25;
            case "A_VECES_ANTES"   -> 0.50;
            case "CASI_SIEMPRE"    -> 0.75;
            case "SIEMPRE_PRIMERO" -> 1.0;
            default                -> 0.0;
        };

        // Pregunta 4: comportamientoImprevistos
        puntos += switch (req.getComportamientoImprevistos().toUpperCase()) {
            case "USA_AHORROS" -> 0.0;
            case "NO_AHORRA"   -> 0.25;
            case "MENOR_AHORRO"-> 0.50;
            case "MISMO_MONTO" -> 0.75;
            case "SIN_PROBLEMA"-> 1.0;
            default            -> 0.0;
        };

        // Pregunta 5: frecuenciaAccionesAhorro
        puntos += switch (req.getFrecuenciaAccionesAhorro().toUpperCase()) {
            case "NUNCA"                -> 0.0;
            case "MENOS_MES"            -> 0.25;
            case "UNA_VEZ_MES"          -> 0.50;
            case "VARIAS_VECES_MES"     -> 0.75;
            case "UNA_VEZ_SEMANA_O_MAS" -> 1.0;
            default                     -> 0.0;
        };

        // Escala: 5 preguntas máx 1 punto c/u → normalizar a escala 4 (para compatibilidad con la respuesta original)
        double escalaMaxima = 4.0;
        double puntosEscalados = (puntos / 5.0) * escalaMaxima;
        puntosEscalados = Math.round(puntosEscalados * 100.0) / 100.0;

        // Clasificar sobre escala 4
        String frecuencia;
        if (puntosEscalados < 1.5) {
            frecuencia = "BAJA";
        } else if (puntosEscalados < 3.0) {
            frecuencia = "MEDIA";
        } else {
            frecuencia = "ALTA";
        }

        return new FrecuenciaAhorroResponse("ENCUESTA", puntosEscalados, escalaMaxima, frecuencia);
    }

    // ─────────────────────────────────────────
    //  Helpers privados
    // ─────────────────────────────────────────

    private String clasificar(int mesesConAhorro) {
        if (mesesConAhorro <= 2) return "BAJA";
        if (mesesConAhorro <= 4) return "MEDIA";
        return "ALTA";
    }
}
