package com.team35.backend.controller;

import com.team35.backend.dto.AnalisisFinancieroRequest;
import com.team35.backend.dto.AnalisisFinancieroResponse;
import com.team35.backend.dto.ClasificacionTransaccionResponse;
import com.team35.backend.dto.TransaccionInputDTO;
import com.team35.backend.service.AnalisisFinancieroService;
import com.team35.backend.service.AuthService;
import com.team35.backend.service.ClasificadorTransaccionesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "FinanceAI", description = "Endpoints de análisis financiero y clasificación de transacciones")
public class AnalisisFinancieroController {

    private final AnalisisFinancieroService analisisFinancieroService;
    private final ClasificadorTransaccionesService clasificadorTransaccionesService;
    private final AuthService authService;

    public AnalisisFinancieroController(AnalisisFinancieroService analisisFinancieroService,
                                        ClasificadorTransaccionesService clasificadorTransaccionesService,
                                        AuthService authService) {
        this.analisisFinancieroService = analisisFinancieroService;
        this.clasificadorTransaccionesService = clasificadorTransaccionesService;
        this.authService = authService;
    }

    /**
     * Analiza la salud financiera. Funciona con o sin JWT:
     * - Con JWT → guarda el análisis en BD y lo asocia al usuario.
     * - Sin JWT (invitado) → devuelve el resultado sin guardar nada.
     */
    @Operation(summary = "Analiza la salud financiera del usuario a partir de 1 a N transacciones")
    @PostMapping("/analisis-financiero")
    public ResponseEntity<AnalisisFinancieroResponse> analizar(@Valid @RequestBody AnalisisFinancieroRequest request) {

        // JWT opcional: intentar obtener el usuarioId si hay un token válido
        Long usuarioId = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                usuarioId = authService.getUsuarioIdAutenticado();
            }
        } catch (Exception e) {
            // No hay usuario autenticado — modo invitado, continuar sin persistir
        }

        return ResponseEntity.ok(analisisFinancieroService.analizar(request, usuarioId));
    }

    @Operation(summary = "Endpoint interno que clasifica una transacción individual en una categoría financiera llamando al modelo de ML")
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
