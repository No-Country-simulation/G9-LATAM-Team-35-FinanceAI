package com.team35.backend.service;

import com.team35.backend.enums.PerfilTipo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio MOCK de generación de recomendaciones.
 * TODO: enriquecer/reemplazar según lo que defina Data Science.
 */
@Service
public class RecomendacionService {

    public List<String> generar(PerfilTipo perfil, String frecuenciaAhorro,
                                 Map<String, Double> resumenGastos) {

        List<String> recomendaciones = new ArrayList<>();

        switch (perfil) {
            case EN_RIESGO -> {
                recomendaciones.add("Reducir gastos no esenciales de forma inmediata");
                recomendaciones.add("Priorizar el pago de deudas para bajar el nivel de endeudamiento");
            }
            case EN_OBSERVACION -> {
                recomendaciones.add("Monitorear gastos recurrentes de categorías no esenciales");
                recomendaciones.add("Aumentar reserva financiera mensual");
            }
            default -> recomendaciones.add("Mantener el buen manejo financiero actual");
        }

        if (frecuenciaAhorro.equalsIgnoreCase("Baja")) {
            recomendaciones.add("Aumentar la frecuencia de ahorro mensual");
        }

        resumenGastos.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> recomendaciones.add(
                        "Revisar el gasto en la categoría " + entry.getKey() + ", es la de mayor peso este período"));

        return recomendaciones;
    }
}
