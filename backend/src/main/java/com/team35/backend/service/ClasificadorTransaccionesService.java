package com.team35.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team35.backend.dto.ClasificacionTransaccionResponse;
import com.team35.backend.dto.TransaccionInputDTO;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


import java.util.Arrays;
import java.util.List;


/**
 * Servicio encargado de comunicarse con el microservicio
 * de Ciencia de Datos desarrollado en FastAPI.
 *
 * Su única responsabilidad es enviar la transacción,
 * esperar la clasificación y devolver el resultado.
 */
@Service
@RequiredArgsConstructor
public class ClasificadorTransaccionesService {


    private final RestTemplate restTemplate;
   //URL del microservicio de Ciencia de Datos (FastAPI) que clasifica transacciones.
    @Value("${python.api.url}")
    private String pythonApiUrl;


    //  Clasificar una sola transacción (envía lista de 1)
    public ClasificacionTransaccionResponse clasificar(TransaccionInputDTO transaccion) {
        List<TransaccionInputDTO> transacciones = List.of(transaccion);
        List<ClasificacionTransaccionResponse> resultados = clasificarMultiples(transacciones);
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    // Metodo que envía la transacción al microservicio de data science y devuelve la clasificación.
    public List<ClasificacionTransaccionResponse> clasificarMultiples(List<TransaccionInputDTO> transacciones) {
        try {

            //  Construir la URL del endpoint de clasificación
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

            if (respuestas != null) {
                return Arrays.asList(respuestas);
            }

            return List.of();

        } catch (Exception e) {
            System.out.println(e.getClass());
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
