package com.team35.backend.service;

import com.team35.backend.dto.AnalisisFinancieroRequest;
import com.team35.backend.dto.AnalisisFinancieroResponse;
import com.team35.backend.entity.Analisis;
import com.team35.backend.entity.Recomendacion;
import com.team35.backend.entity.Usuario;
import com.team35.backend.enums.PerfilTipo;
import com.team35.backend.repository.AnalisisRepository;
import com.team35.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class AnalisisFinancieroService {

    private final RestTemplate restTemplate;
    private final AnalisisRepository analisisRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${python.api.url}")
    private String pythonApiUrl;

    public AnalisisFinancieroService(
            RestTemplate restTemplate,
            AnalisisRepository analisisRepository,
            UsuarioRepository usuarioRepository) {
        this.restTemplate = restTemplate;
        this.analisisRepository = analisisRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Analiza la salud financiera y persiste si usuarioId no es null.
     */
    @Transactional
    public AnalisisFinancieroResponse analizar(AnalisisFinancieroRequest request, Long usuarioId) {
        // 1. Llamar a Python API
        String url = pythonApiUrl + "/analisis-financiero";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AnalisisFinancieroRequest> entity = new HttpEntity<>(request, headers);

        AnalisisFinancieroResponse response = restTemplate.postForObject(url, entity, AnalisisFinancieroResponse.class);

        // 2. Persistir SOLO si usuarioId no es null
        if (usuarioId != null && response != null) {
            persistirAnalisis(request, response, usuarioId);
        }

        return response;
    }

    /**
     * Sobrecarga para invitados (sin persistencia).
     */
    public AnalisisFinancieroResponse analizar(AnalisisFinancieroRequest request) {
        return analizar(request, null);
    }

    private void persistirAnalisis(AnalisisFinancieroRequest request,
                                   AnalisisFinancieroResponse response,
                                   Long usuarioId) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return;
        }

        // Convertir perfil de texto a ENUM (Si Python devuelve "En observación", "Saludable", etc.)
        PerfilTipo perfilEnum = convertirTextoAPerfil(response.getPerfilFinanciero());

        Analisis analisis = Analisis.builder()
                .usuario(usuarioOpt.get())
                .perfil(perfilEnum)
                .probabilidad(BigDecimal.valueOf(response.getProbabilidad()))
                .ingresoMensual(BigDecimal.valueOf(request.getIngresoMensual()))
                .nivelEndeudamiento(BigDecimal.valueOf(request.getNivelEndeudamiento()))
                .frecuenciaAhorro(request.getFrecuenciaAhorro())
                .fechaAnalisis(ZonedDateTime.now(ZoneId.of("America/Mexico_City")).toLocalDateTime())
                .build();

        // Agregar recomendaciones
        if (response.getRecomendaciones() != null) {
            for (String texto : response.getRecomendaciones()) {
                Recomendacion rec = Recomendacion.builder()
                        .texto(texto)
                        .build();
                analisis.agregarRecomendacion(rec);
            }
        }

        analisisRepository.save(analisis);
    }


    private PerfilTipo convertirTextoAPerfil(String texto) {
        if (texto == null) return PerfilTipo.EN_OBSERVACION;

        String lower = texto.toLowerCase();
        if (lower.contains("saludable")) return PerfilTipo.SALUDABLE;
        if (lower.contains("riesgo")) return PerfilTipo.EN_RIESGO;
        if (lower.contains("observacion") || lower.contains("observación")) return PerfilTipo.EN_OBSERVACION;

        return PerfilTipo.EN_OBSERVACION;
    }
}