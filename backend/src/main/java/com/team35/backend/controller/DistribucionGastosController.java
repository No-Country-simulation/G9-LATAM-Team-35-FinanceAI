package com.team35.backend.controller;

import com.team35.backend.service.DistribucionGastosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.Map;

@RestController
@Tag(name = "Distribución de gastos", description = "Gastos ya clasificados, agrupados por categoría, para la gráfica del frontend")
public class DistribucionGastosController {

    private final DistribucionGastosService distribucionGastosService;

    public DistribucionGastosController(DistribucionGastosService distribucionGastosService) {
        this.distribucionGastosService = distribucionGastosService;
    }

    // TODO: reemplazar el parámetro usuarioId por el usuario autenticado (JWT)
    // en cuanto esté listo el filtro de seguridad. Por ahora se recibe explícito
    // para poder probar sin depender de auth.
    @Operation(summary = "Distribución de gastos por categoría en un mes específico. "
            + "Devuelve null si no hay transacciones clasificadas ese mes.")
    @GetMapping("/distribucion-gastos")
    public ResponseEntity<Map<String, Double>> obtenerDistribucion(
            @RequestParam Long usuarioId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {

        Map<String, Double> distribucion = distribucionGastosService.obtenerDistribucion(usuarioId, mes);
        return ResponseEntity.ok(distribucion); // ok(null) -> 200 con body JSON "null", a propósito
    }
}
