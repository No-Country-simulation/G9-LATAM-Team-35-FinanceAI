package com.team35.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración del cliente HTTP utilizado para comunicarse con el
 * microservicio Python (Data Science).
 * <p>
 * IMPORTANTE: "new RestTemplate()" a secas NO usa el ObjectMapper configurado
 * en application.properties (spring.jackson.property-naming-strategy=SNAKE_CASE).
 * Sin este ajuste, los campos de varias palabras (ingresoMensual, etc.) se
 * mandarían en camelCase a Data en vez de snake_case, y fallaría la validación
 * del lado de Python. Con clasificar-transaccion no se nota porque sus campos
 * son de una sola palabra (descripcion, valor) — pero con análisis sí revienta.
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, converter);
        return restTemplate;
    }
}
