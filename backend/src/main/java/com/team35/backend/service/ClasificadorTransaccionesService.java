package com.team35.backend.service;

import com.team35.backend.dto.ClasificacionTransaccionResponse;
import com.team35.backend.dto.TransaccionInputDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.team35.backend.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Servicio encargado de comunicarse con el microservicio
 * de Ciencia de Datos desarrollado en FastAPI.
 *
 * Incluye un fallback heurístico local si el microservicio Python no está disponible.
 */
@Service
@RequiredArgsConstructor
public class ClasificadorTransaccionesService {

    private final RestTemplate restTemplate;

    // URL del microservicio de Ciencia de Datos (FastAPI) que clasifica transacciones.
    @Value("${python.api.url}")
    private String pythonApiUrl;

    // Clasificar una sola transacción (envía lista de 1)
    public ClasificacionTransaccionResponse clasificar(TransaccionInputDTO transaccion) {
        List<TransaccionInputDTO> transacciones = List.of(transaccion);
        List<ClasificacionTransaccionResponse> resultados = clasificarMultiples(transacciones);
        return resultados.isEmpty() ? new ClasificacionTransaccionResponse(transaccion.getDescripcion(), transaccion.getValor(), "otros") : resultados.get(0);
    }

    // Método que envía las transacciones al microservicio de data science y devuelve la clasificación.
    public List<ClasificacionTransaccionResponse> clasificarMultiples(List<TransaccionInputDTO> transacciones) {
        try {
            // Construir la URL del endpoint de clasificación
            String url = pythonApiUrl + "/clasificar-transaccion";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<TransaccionInputDTO>> request = new HttpEntity<>(transacciones, headers);

            // Esperar un array de respuestas
            ResponseEntity<ClasificacionTransaccionResponse[]> response =
                    restTemplate.postForEntity(
                            url,
                            request,
                            ClasificacionTransaccionResponse[].class
                    );

            ClasificacionTransaccionResponse[] respuestas = response.getBody();

            if (respuestas != null && respuestas.length > 0) {
                return Arrays.stream(respuestas)
                        .peek(respuesta -> respuesta.setCategoria_gasto(
                                StringUtils.normalizar(respuesta.getCategoria_gasto())
                        ))
                        .toList();
            }

            return List.of();

        } catch (Exception e) {
            System.err.println("[FALLBACK RESILIENTE] Microservicio Python no disponible (" + e.getMessage() + "). Usando clasificador heurístico local.");
            return transacciones.stream()
                    .map(this::clasificarLocalmente)
                    .toList();
        }
    }

    /**
     * Fallback heurístico local por reglas de palabras clave si FastAPI está caído o iniciando.
     */
    private ClasificacionTransaccionResponse clasificarLocalmente(TransaccionInputDTO tx) {
        String desc = tx.getDescripcion() != null ? tx.getDescripcion().toLowerCase() : "";
        String cat;
        if (desc.contains("vivienda") || desc.contains("renta") || desc.contains("luz") || desc.contains("agua") || desc.contains("servicio") || desc.contains("internet") || desc.contains("electricidad") || desc.contains("gas")) {
            cat = "vivienda y servicios";
        } else if (desc.contains("comida") || desc.contains("super") || desc.contains("alimento") || desc.contains("restaurante") || desc.contains("cena") || desc.contains("desayuno") || desc.contains("almuerzo") || desc.contains("mercado")) {
            cat = "alimentacion";
        } else if (desc.contains("netflix") || desc.contains("spotify") || desc.contains("cine") || desc.contains("ocio") || desc.contains("entretenimiento") || desc.contains("juego") || desc.contains("fiesta") || desc.contains("bar") || desc.contains("steam")) {
            cat = "ocio y entretenimiento";
        } else if (desc.contains("uber") || desc.contains("taxi") || desc.contains("gasolina") || desc.contains("transporte") || desc.contains("metro") || desc.contains("bus") || desc.contains("didi") || desc.contains("peaje")) {
            cat = "transporte";
        } else if (desc.contains("medico") || desc.contains("farmacia") || desc.contains("salud") || desc.contains("hospital") || desc.contains("doctor") || desc.contains("dentista")) {
            cat = "salud";
        } else if (desc.contains("deuda") || desc.contains("prestamo") || desc.contains("tarjeta") || desc.contains("credito") || desc.contains("hipoteca")) {
            cat = "deudas";
        } else {
            cat = "otros";
        }
        return new ClasificacionTransaccionResponse(tx.getDescripcion(), tx.getValor(), cat);
    }
}
