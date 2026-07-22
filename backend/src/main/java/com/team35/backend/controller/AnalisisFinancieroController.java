package com.team35.backend.controller;

import com.team35.backend.dto.AnalisisFinancieroRequest;
import com.team35.backend.dto.AnalisisFinancieroResponse;
import com.team35.backend.dto.ClasificacionTransaccionResponse;
import com.team35.backend.dto.TransaccionInputDTO;
import com.team35.backend.service.AnalisisFinancieroService;
import com.team35.backend.service.ClasificadorTransaccionesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "FinanceAI", description = "Endpoints de análisis financiero y clasificación de transacciones")
public class AnalisisFinancieroController {

    private final AnalisisFinancieroService analisisFinancieroService;
    private final ClasificadorTransaccionesService clasificadorTransaccionesService;

    public AnalisisFinancieroController(AnalisisFinancieroService analisisFinancieroService,
                                         ClasificadorTransaccionesService clasificadorTransaccionesService) {
        this.analisisFinancieroService = analisisFinancieroService;
        this.clasificadorTransaccionesService = clasificadorTransaccionesService;
    }

    @Operation(summary = "Analiza la salud financiera del usuario a partir de 1 a N transacciones")
    @PostMapping("/analisis-financiero")
    public ResponseEntity<AnalisisFinancieroResponse> analizar(@Valid @RequestBody AnalisisFinancieroRequest request) {
        return ResponseEntity.ok(analisisFinancieroService.analizar(request));
    }

    @Operation(summary = "Clasifica una transacción individual en una categoría financiera")
    @PostMapping("/clasificar-transaccion")
    public ResponseEntity<ClasificacionTransaccionResponse> clasificar(@Valid @RequestBody TransaccionInputDTO transaccion) {
        return ResponseEntity.ok(clasificadorTransaccionesService.clasificar(transaccion));
    }

    @Operation(summary = "Verifica que la API esté activa")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FinanceAI API activa");
    }
}
