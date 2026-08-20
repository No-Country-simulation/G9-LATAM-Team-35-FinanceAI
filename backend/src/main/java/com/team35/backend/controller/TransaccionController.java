package com.team35.backend.controller;

import com.team35.backend.dto.*;
import com.team35.backend.enums.TipoTransaccion;
import com.team35.backend.entity.Usuario;
import com.team35.backend.repository.UsuarioRepository;
import com.team35.backend.service.AuthService;
import com.team35.backend.service.ClasificadorTransaccionesService;
import com.team35.backend.service.TransaccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService transaccionService;
    private final AuthService authService;
    private final ClasificadorTransaccionesService clasificadorTransaccionesService;

    @PostMapping("/clasificar-transaccion")
    public ResponseEntity<List<ClasificacionTransaccionResponse>>  clasificar(
            @Valid @RequestBody List<TransaccionInputDTO> transacciones
    ) {
        List<ClasificacionTransaccionResponse> respuestas =
                clasificadorTransaccionesService.clasificarMultiples(transacciones);
        return ResponseEntity.ok(respuestas);
    }

    @Operation(summary = "Registra una nueva transacción para un usuario específico")
    @PostMapping
    public ResponseEntity<TransaccionDetails> registrar(
            @Valid
            @RequestBody
            TransaccionRegister datos
    ) {
        Usuario usuario = authService.getUsuarioAutenticado();
        TransaccionDetails respuesta = transaccionService.registrarTransaccion(datos, usuario);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    @Operation(summary = "Obtiene todas las transacciones de un usuario específico")
    @GetMapping
    public ResponseEntity<List<TransaccionDetails>>
    obtenerTransacciones() {
        Long usuarioId = authService.getUsuarioIdAutenticado();
        List<TransaccionDetails> transacciones =
                transaccionService
                        .obtenerTransacciones(
                                usuarioId
                        );
        //imprimir lo que se va a devolver, pero con el formato json correcto de lista para que se pueda debuguear bien
        System.out.println("Transacciones obtenidas: " + transacciones.toString() );


        return ResponseEntity.ok(
                transacciones
        );
    }

    @Operation(summary = "Obtiene todas las transacciones de un usuario específico filtradas por tipo")
    @GetMapping("/tipo")
    public ResponseEntity<List<TransaccionDetails>>
    obtenerPorTipo(
            @RequestParam
            TipoTransaccion tipo
    ) {
        Long usuarioId = authService.getUsuarioIdAutenticado();
        List<TransaccionDetails> transacciones =
                transaccionService
                        .obtenerTransaccionesPorTipo(
                                usuarioId,
                                tipo
                        );

        return ResponseEntity.ok(
                transacciones
        );
    }


    //OBTENER TRANSACCIONES SIN CLASIFICAR
    @Operation(summary = "Obtiene todas las transacciones de un usuario específico que aún no han sido clasificadas")
    @GetMapping("/sin-clasificar")
    public ResponseEntity<List<TransaccionDetails>>
    obtenerSinClasificar() {
        Long usuarioId = authService.getUsuarioIdAutenticado();
        List<TransaccionDetails> transacciones =
                transaccionService
                        .obtenerTransaccionesSinClasificar(
                                usuarioId
                        );
        return ResponseEntity.ok(
                transacciones
        );
    }

    @Operation(summary="Elimina una transacción específica de un usuario")
    @DeleteMapping("/{transaccionId}")
    public ResponseEntity<Map<String, String>> eliminarTransaccion(
            @PathVariable Long transaccionId
    ) {
        Long usuarioId = authService.getUsuarioIdAutenticado();
        transaccionService.eliminarTransaccion(transaccionId, usuarioId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Transacción eliminada correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Edita una transacción específica de un usuario")
    @PutMapping("/{transaccionId}")
    public ResponseEntity<TransaccionDetails> editarTransaccion(
            @PathVariable Long transaccionId,
            @Valid @RequestBody TransaccionRegister datos
    ) {
        Long usuarioId = authService.getUsuarioIdAutenticado();
        TransaccionDetails transaccionEditada = transaccionService.editarTransaccion(transaccionId, usuarioId, datos);
        return ResponseEntity.ok(transaccionEditada);
    }

    @Operation(summary = "Busca transacciones del usuario por descripción")
    @GetMapping("/buscar")
    public ResponseEntity<List<TransaccionDetails>> buscarPorDescripcion(
            @RequestParam String descripcion
    ) {
        Long usuarioId = authService.getUsuarioIdAutenticado();

        List<TransaccionDetails> transacciones = transaccionService.buscarPorDescripcion( usuarioId, descripcion);
        return ResponseEntity.ok(transacciones);
    }

    @Operation(summary = "Calcula el ingreso mensual del usuario para usarlo en el análisis financiero")
    @GetMapping("/ingreso-mensual")
    public ResponseEntity<IngresoMensualDetails> calcularIngresoMensual(
            @RequestParam int mes,
            @RequestParam int anio
    ) {
        Long usuarioId = authService.getUsuarioIdAutenticado();

        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException(
                    "El mes debe estar entre 1 y 12"
            );
        }

        if (anio < 1800 || anio > 2200) {
            throw new IllegalArgumentException(
                    "El año debe estar entre 1800 y 2200"
            );
        }
        IngresoMensualDetails respuesta =
                transaccionService.calcularIngresoMensual(usuarioId, mes, anio);

        return ResponseEntity.ok(respuesta);
    }

}