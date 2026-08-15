package com.team35.backend.controller;

import com.team35.backend.dto.AnalisisDetails;
import com.team35.backend.service.AnalisisService;
import com.team35.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analisis")
@RequiredArgsConstructor
public class AnalisisController {

    private final AnalisisService analisisService;
    private final AuthService authService;

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