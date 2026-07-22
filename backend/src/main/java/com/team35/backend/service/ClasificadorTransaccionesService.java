package com.team35.backend.service;

import com.team35.backend.dto.ClasificacionTransaccionResponse;
import com.team35.backend.dto.TransaccionInputDTO;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Servicio MOCK de clasificación de transacciones.
 * TODO: reemplazar por la llamada al prediction_wrapper.py (Fase 9 de Data Science),
 * probablemente vía un microservicio Python o OCI Functions.
 */
@Service
public class ClasificadorTransaccionesService {

    private static final Map<String, String> PALABRAS_CLAVE = new LinkedHashMap<>();

    static {
        PALABRAS_CLAVE.put("supermercado", "ALIMENTACION");
        PALABRAS_CLAVE.put("restaurante", "ALIMENTACION");
        PALABRAS_CLAVE.put("comida", "ALIMENTACION");
        PALABRAS_CLAVE.put("mercado", "ALIMENTACION");

        PALABRAS_CLAVE.put("combustible", "TRANSPORTE");
        PALABRAS_CLAVE.put("gasolina", "TRANSPORTE");
        PALABRAS_CLAVE.put("uber", "TRANSPORTE");
        PALABRAS_CLAVE.put("taxi", "TRANSPORTE");
        PALABRAS_CLAVE.put("bus", "TRANSPORTE");

        PALABRAS_CLAVE.put("farmacia", "SALUD");
        PALABRAS_CLAVE.put("medico", "SALUD");
        PALABRAS_CLAVE.put("hospital", "SALUD");
        PALABRAS_CLAVE.put("clinica", "SALUD");

        PALABRAS_CLAVE.put("alquiler", "VIVIENDA");
        PALABRAS_CLAVE.put("renta", "VIVIENDA");
        PALABRAS_CLAVE.put("hipoteca", "VIVIENDA");

        PALABRAS_CLAVE.put("universidad", "EDUCACION");
        PALABRAS_CLAVE.put("curso", "EDUCACION");
        PALABRAS_CLAVE.put("colegio", "EDUCACION");
        PALABRAS_CLAVE.put("libros", "EDUCACION");

        PALABRAS_CLAVE.put("streaming", "OCIO");
        PALABRAS_CLAVE.put("cine", "OCIO");
        PALABRAS_CLAVE.put("netflix", "OCIO");
        PALABRAS_CLAVE.put("spotify", "OCIO");

        PALABRAS_CLAVE.put("luz", "SERVICIOS");
        PALABRAS_CLAVE.put("agua", "SERVICIOS");
        PALABRAS_CLAVE.put("internet", "SERVICIOS");
        PALABRAS_CLAVE.put("telefono", "SERVICIOS");
        PALABRAS_CLAVE.put("electricidad", "SERVICIOS");
    }

    public ClasificacionTransaccionResponse clasificar(TransaccionInputDTO transaccion) {
        String categoria = inferirCategoria(transaccion.getDescripcion());
        double confianza = categoria.equals("OTROS") ? 0.40 : 0.85;
        return new ClasificacionTransaccionResponse(
                transaccion.getDescripcion(),
                transaccion.getValor(),
                categoria,
                confianza
        );
    }

    public String inferirCategoria(String descripcion) {
        String texto = descripcion.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> entrada : PALABRAS_CLAVE.entrySet()) {
            if (texto.contains(entrada.getKey())) {
                return entrada.getValue();
            }
        }
        return "OTROS";
    }
}
