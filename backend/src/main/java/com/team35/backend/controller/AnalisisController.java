package com.team35.backend.controller;

import com.team35.backend.dto.AnalisisDetails;
import com.team35.backend.dto.EndeudamientoRequest;
import com.team35.backend.dto.EndeudamientoDetails;
import com.team35.backend.service.AnalisisService;
import com.team35.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analisis")
@RequiredArgsConstructor
@Validated
public class AnalisisController {

    private final AnalisisService analisisService;
    private final AuthService authService;

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

}