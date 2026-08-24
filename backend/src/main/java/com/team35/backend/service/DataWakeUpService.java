package com.team35.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DataWakeUpService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${python.api.url}")
    private String pythonUrl;

    @Async
    public void despertarData() {
        System.out.println(">>> Intentando despertar Data...");

        try {
            String url = pythonUrl + "/health";
            System.out.println(">>> Llamando a: " + url);
            restTemplate.getForEntity(url, String.class);

            System.out.println("Data está disponible.");

        } catch (Exception e) {

            System.out.println(
                    "Data no respondió. Backend continuará funcionando."
            );
        }
    }
}