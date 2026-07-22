package com.team35.backend.service;

import com.team35.backend.enums.PerfilTipo;
import org.springframework.stereotype.Service;

/**
 * Servicio MOCK de evaluación de perfil financiero.
 * TODO: reemplazar por el modelo_perfil_financiero.joblib/pkl (Fase 8 de Data Science).
 */
@Service
public class PerfilFinancieroService {

    public static class ResultadoPerfil {
        public final PerfilTipo perfil;
        public final double probabilidad;

        public ResultadoPerfil(PerfilTipo perfil, double probabilidad) {
            this.perfil = perfil;
            this.probabilidad = probabilidad;
        }
    }

    public ResultadoPerfil evaluar(double ingresoMensual, double nivelEndeudamiento,
                                    String frecuenciaAhorro, double totalGastos) {

        double porcentajeGastado = ingresoMensual > 0 ? (totalGastos / ingresoMensual) * 100 : 100;
        String ahorro = frecuenciaAhorro.toLowerCase();

        if (nivelEndeudamiento >= 50 || porcentajeGastado >= 80 || ahorro.equals("baja")) {
            return new ResultadoPerfil(PerfilTipo.EN_RIESGO, 0.78);
        }

        if (nivelEndeudamiento >= 20 || porcentajeGastado >= 50 || ahorro.equals("media")) {
            return new ResultadoPerfil(PerfilTipo.EN_OBSERVACION, 0.82);
        }

        return new ResultadoPerfil(PerfilTipo.SALUDABLE, 0.88);
    }
}
