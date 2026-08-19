package com.team35.backend.controller;

import com.team35.backend.dto.DistribucionGastosResponse;
import com.team35.backend.service.AuthService;
import com.team35.backend.service.DistribucionGastosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Distribución de gastos", description = "Gastos ya clasificados, agrupados por categoría, para la gráfica del frontend")
public class DistribucionGastosController {

    private final DistribucionGastosService distribucionGastosService;
    private final AuthService authService;

    public DistribucionGastosController(DistribucionGastosService distribucionGastosService, AuthService authService) {
        this.distribucionGastosService = distribucionGastosService;
        this.authService = authService;
    }

    @Operation(summary = "Distribución de gastos por categoría en un mes específico, del usuario autenticado "
            + "(Página 2, Opción A). Devuelve null si no hay transacciones clasificadas ese mes.")
    @GetMapping("/api/distribucion-gastos")
    public ResponseEntity<DistribucionGastosResponse> obtenerDistribucion(
            @RequestParam int mes,
            @RequestParam int anio) {

        Long usuarioId = authService.getUsuarioIdAutenticado();
        DistribucionGastosResponse distribucion = distribucionGastosService.obtenerDistribucion(usuarioId, mes, anio);
        return ResponseEntity.ok(distribucion); // ok(null) -> 200 con body JSON "null", a propósito
    }
}
