package com.team35.backend.service;

import com.team35.backend.dto.AnalisisFinancieroRequest;
import com.team35.backend.dto.AnalisisFinancieroResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Ya NO calcula nada por su cuenta. prediction_wrapper.py (Data) expone su
 * propio POST /analisis-financiero que clasifica + calcula perfil + genera
 * recomendaciones en una sola llamada — este service solo reenvía la
 * petición tal cual y regresa la respuesta de Python sin transformarla.
 * <p>
 * IMPORTANTE: NO se traduce ni se cambia el texto/formato del perfil
 * (EN_OBSERVACION vs "En observación" vs "en observación") — se pasa
 * exactamente lo que Python responda. Si el formato real de Python no
 * coincide con lo documentado en Notion, es algo a resolver con Data,
 * no algo que este service deba adivinar o corregir silenciosamente.
 */
@Service
public class AnalisisFinancieroService {

    private final RestTemplate restTemplate;

    @Value("${python.api.url}")
    private String pythonApiUrl;

    public AnalisisFinancieroService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AnalisisFinancieroResponse analizar(AnalisisFinancieroRequest request) {
        String url = pythonApiUrl + "/analisis-financiero";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AnalisisFinancieroRequest> entity = new HttpEntity<>(request, headers);

        return restTemplate.postForObject(url, entity, AnalisisFinancieroResponse.class);
    }
}
