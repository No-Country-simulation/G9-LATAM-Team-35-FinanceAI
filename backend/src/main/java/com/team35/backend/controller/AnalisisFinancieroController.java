package com.team35.backend.controller;

import com.team35.backend.dto.AnalisisFinancieroRequest;
import com.team35.backend.dto.AnalisisFinancieroResponse;
import com.team35.backend.dto.ClasificacionTransaccionResponse;
import com.team35.backend.dto.TransaccionInputDTO;
import com.team35.backend.entity.Usuario;
import com.team35.backend.service.AnalisisFinancieroService;
import com.team35.backend.service.AuthService;
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
    private final AuthService authService;

    public AnalisisFinancieroController(AnalisisFinancieroService analisisFinancieroService,
                                         ClasificadorTransaccionesService clasificadorTransaccionesService,
                                         AuthService authService) {
        this.analisisFinancieroService = analisisFinancieroService;
        this.clasificadorTransaccionesService = clasificadorTransaccionesService;
        this.authService = authService;
    }

    @Operation(summary = "Analiza la salud financiera del usuario a partir de 1 a N transacciones. "
            + "Funciona sin cuenta (no se guarda nada); si hay un usuario autenticado, se guarda el historial.")
    @PostMapping("/api/analisis-financiero")
    public ResponseEntity<AnalisisFinancieroResponse> analizar(@Valid @RequestBody AnalisisFinancieroRequest request) {

        AnalisisFinancieroResponse response = analisisFinancieroService.analizar(request);

        if (authService.isAuthenticated()) {
            Usuario usuario = authService.getUsuarioAutenticado();
            // TODO: persistir en "analisis" + "recomendaciones" para el usuario autenticado.
            // Todavía pendiente: falta wirear AnalisisRepository/RecomendacionRepository aquí,
            // y normalizar el texto de perfil que regrese Python al enum PerfilTipo antes de guardar.
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Clasifica una transacción individual en una categoría financiera")
    @PostMapping("/api/clasificar-transaccion")
    public ResponseEntity<ClasificacionTransaccionResponse> clasificar(@Valid @RequestBody TransaccionInputDTO transaccion) {
        return ResponseEntity.ok(clasificadorTransaccionesService.clasificar(transaccion));
    }

    @Operation(summary = "Verifica que la API esté activa")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FinanceAI API activa");
    }
}
