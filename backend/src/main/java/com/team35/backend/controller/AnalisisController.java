package com.team35.backend.controller;

import com.team35.backend.dto.AnalisisDetails;
import com.team35.backend.dto.EndeudamientoRequest;
import com.team35.backend.dto.EndeudamientoDetails;
import com.team35.backend.dto.FrecuenciaAhorroEncuestaRequest;
import com.team35.backend.dto.FrecuenciaAhorroResponse;
import com.team35.backend.service.AnalisisService;
import com.team35.backend.service.AuthService;
import com.team35.backend.service.FrecuenciaAhorroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Análisis financiero", description = "Endpoints relacionados con el análisis financiero del usuario")
@RequestMapping("/api/analisis")
@RequiredArgsConstructor
@Validated
public class AnalisisController {

    private final AnalisisService analisisService;
    private final AuthService authService;
    private final FrecuenciaAhorroService frecuenciaAhorroService;
    private final com.team35.backend.service.AlertaService alertaService;

    @Operation(summary = "Obtiene las alertas financieras en tiempo real para el período seleccionado")
    @GetMapping("/alertas")
    public ResponseEntity<List<com.team35.backend.dto.AlertaDTO>> obtenerAlertas(
            @RequestParam(required = false) String mes
    ) {
        Long usuarioId = authService.getUsuarioIdAutenticado();
        List<com.team35.backend.dto.AlertaDTO> alertas = alertaService.generarAlertasUsuario(usuarioId, mes);
        return ResponseEntity.ok(alertas);
    }

    @Operation(summary="Obtiene el nivel de endeudamiento calculado a partir de los datos enviados por el usuario que servirán para generar un análisis financiero")
    @PostMapping("/nivel-endeudamiento")
    public ResponseEntity<EndeudamientoDetails> calcularEndeudamiento(
            @Valid @RequestBody EndeudamientoRequest request
    ) {
        // Agrega este log para ver qué está llegando
        System.out.println("Ingreso: " + request.getIngresoMensual());
        System.out.println("Cuotas: " + request.getCuotasMensuales());
        EndeudamientoDetails response = analisisService.calcularEndeudamiento(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtiene el historial de análisis financieros del usuario autenticado"
    )
    @GetMapping("/historial")
    public ResponseEntity<List<AnalisisDetails>> obtenerHistorial() {

        Long usuarioId = authService.getUsuarioIdAutenticado();

        List<AnalisisDetails> historial =
                analisisService.obtenerHistorial(usuarioId);

        return ResponseEntity.ok(historial);
    }

    @Operation(summary = "Busca análisis por nombre generado (fecha + perfil)")
    @GetMapping("/buscar")
    public ResponseEntity<List<AnalisisDetails>> buscarAnalisis(
            @RequestParam String query
    ) {
        Long usuarioId = authService.getUsuarioIdAutenticado();
        List<AnalisisDetails> resultados = analisisService.buscarAnalisis(usuarioId, query);
        return ResponseEntity.ok(resultados);
    }

    @Operation(
            summary = "Calcula la frecuencia de ahorro del usuario autenticado (híbrido: automático si hay ≥3 meses de historial, encuesta si no)"
    )
    @GetMapping("/frecuencia-ahorro")
    public ResponseEntity<FrecuenciaAhorroResponse> obtenerFrecuenciaAhorro() {
        Long usuarioId = authService.getUsuarioIdAutenticado();
        FrecuenciaAhorroResponse response;
        if (frecuenciaAhorroService.tieneSuficienteHistorial(usuarioId)) {
            response = frecuenciaAhorroService.calcularDesdeHistorial(usuarioId);
        } else {
            // No hay suficiente historial: devolvemos BAJA como default con indicación de que debe hacer la encuesta
            response = new FrecuenciaAhorroResponse("ENCUESTA_PENDIENTE", "BAJA", 0);
            response.setMensaje("Aún no tienes suficiente historial. Completa la encuesta para obtener tu frecuencia de ahorro.");
        }
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Calcula la frecuencia de ahorro mediante la encuesta de 5 preguntas (disponible para usuarios autenticados e invitados)"
    )
    @PostMapping("/frecuencia-ahorro-encuesta")
    public ResponseEntity<FrecuenciaAhorroResponse> calcularFrecuenciaEncuesta(
            @Valid @RequestBody FrecuenciaAhorroEncuestaRequest request
    ) {
        FrecuenciaAhorroResponse response = frecuenciaAhorroService.calcularDesdeEncuesta(request);
        return ResponseEntity.ok(response);
    }

}