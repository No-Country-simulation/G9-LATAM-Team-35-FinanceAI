package com.team35.backend.util;

import com.team35.backend.enums.PerfilTipo;

import java.util.Map;

/**
 * Traduce el valor del enum PerfilTipo (el que se guarda en BD, ej. EN_OBSERVACION)
 * al texto legible que espera el contrato de la API (ej. "En observación"),
 * según lo acordado con Data Science.
 */
public final class PerfilTextoMapper {

    private static final Map<PerfilTipo, String> TEXTOS = Map.of(
            PerfilTipo.SALUDABLE, "Saludable",
            PerfilTipo.EN_OBSERVACION, "En observación",
            PerfilTipo.EN_RIESGO, "En riesgo"
    );

    private PerfilTextoMapper() {
    }

    public static String aTexto(PerfilTipo perfil) {
        return TEXTOS.getOrDefault(perfil, perfil.name());
    }
}
