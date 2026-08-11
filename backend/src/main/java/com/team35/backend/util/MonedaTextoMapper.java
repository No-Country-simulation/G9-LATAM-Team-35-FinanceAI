package com.team35.backend.util;

import com.team35.backend.enums.Moneda;

import java.util.Map;

public final class MonedaTextoMapper {

    private static final Map<Moneda, String> TEXTOS = Map.of(
            Moneda.MXN, "Peso Mexicano",
            Moneda.USD, "Dólar estadounidense",
            Moneda.EUR, "Euro",
            Moneda.GTQ, "Quetzal guatemalteco",
            Moneda.HNL, "Lempira hondureño"
    );

    private MonedaTextoMapper() {
    }

    public static String aTexto(Moneda moneda) {
        return TEXTOS.getOrDefault(moneda, moneda.name());
    }
}
