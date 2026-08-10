package com.team35.backend.util;

import java.text.Normalizer;

public class StringUtils {

    //Elimina acentos y caracteres especiales de un texto. Ejemplo: "Alimentación" → "ALIMENTACION"

    public static String normalizar(String texto) {
        if (texto == null) {
            return null;
        }
        // Normalizar y eliminar acentos
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        normalizado = normalizado.replaceAll("\\p{M}", "");
        return normalizado.toUpperCase().trim();
    }
}