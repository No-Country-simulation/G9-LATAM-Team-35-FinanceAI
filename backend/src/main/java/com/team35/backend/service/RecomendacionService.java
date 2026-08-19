package com.team35.backend.service;

import com.team35.backend.enums.PerfilTipo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio de generación de recomendaciones financieras personalizadas.
 * Alineado con las reglas e indicadores de Data Science.
 */
@Service
public class RecomendacionService {

    public List<String> generar(PerfilTipo perfil, String frecuenciaAhorro,
                                 Map<String, Double> resumenGastos) {

        List<String> recomendaciones = new ArrayList<>();

        // 1. Recomendaciones según el Perfil Financiero
        switch (perfil) {
            case EN_RIESGO -> {
                recomendaciones.add("Prioriza la reducción de gastos no esenciales para recuperar estabilidad financiera.");
                recomendaciones.add("Evita adquirir nuevas líneas de crédito hasta mejorar tu capacidad de ahorro.");
                recomendaciones.add("Construye un fondo de emergencia equivalente a por lo menos tres meses de gastos fijos.");
            }
            case EN_OBSERVACION -> {
                recomendaciones.add("Mantén un seguimiento mensual de tus gastos para evitar deteriorar tu situación financiera.");
                recomendaciones.add("Procura destinar al menos el 10% al 15% de tus ingresos al ahorro antes de realizar gastos variables.");
            }
            default -> {
                recomendaciones.add("Mantén tus hábitos financieros actuales y tu disciplina de presupuesto.");
                recomendaciones.add("Considera diversificar parte de tu ahorro mediante instrumentos de inversión de bajo riesgo o renta fija.");
            }
        }

        // 2. Recomendaciones según la Frecuencia de Ahorro
        if (frecuenciaAhorro != null) {
            String freq = frecuenciaAhorro.trim().toLowerCase();
            if (freq.equals("baja")) {
                recomendaciones.add("Establece una transferencia automática de ahorro a principio de mes para generar hábito constante.");
            } else if (freq.equals("media")) {
                recomendaciones.add("Incrementa gradualmente tu meta de ahorro mensual para consolidar un colchón financiero más sólido.");
            } else if (freq.equals("alta")) {
                recomendaciones.add("Tu constancia de ahorro es destacada; evalúa metas de mediano plazo como fondos indexados.");
            }
        }

        // 3. Recomendaciones específicas según la Categoría con Mayor Gasto
        if (resumenGastos != null && !resumenGastos.isEmpty()) {
            resumenGastos.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .ifPresent(entry -> {
                        String cat = entry.getKey().toLowerCase();
                        if (cat.contains("ocio") || cat.contains("entretenimiento")) {
                            recomendaciones.add("Define un presupuesto tope mensual para actividades recreativas y salidas de entretenimiento.");
                        } else if (cat.contains("alimentacion") || cat.contains("comida")) {
                            recomendaciones.add("Planifica las compras semanales del hogar para optimizar y reducir gastos impulsivos en alimentación.");
                        } else if (cat.contains("vivienda") || cat.contains("servicio")) {
                            recomendaciones.add("Revisa periódicamente los costos asociados a servicios y suscripciones del hogar para identificar ahorros.");
                        } else if (cat.contains("transporte")) {
                            recomendaciones.add("Evalúa alternativas de movilidad y rutas combinadas para reducir el costo mensual de desplazamientos.");
                        } else if (cat.contains("deuda") || cat.contains("credito")) {
                            recomendaciones.add("Aplica el método de bola de nieve o avalancha para liquidar primero tus compromisos crediticios más costosos.");
                        } else {
                            recomendaciones.add("La categoría '" + entry.getKey() + "' representa tu mayor egreso este período; audita sus movimientos individuales.");
                        }
                    });
        }

        return recomendaciones;
    }
}
