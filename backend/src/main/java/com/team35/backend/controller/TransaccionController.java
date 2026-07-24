package com.team35.backend.controller;

import com.team35.backend.dto.TransaccionDetails;
import com.team35.backend.dto.TransaccionRegister;
import com.team35.backend.enums.TipoTransaccion;
import com.team35.backend.entity.Usuario;
import com.team35.backend.repository.UsuarioRepository;
import com.team35.backend.service.TransaccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService transaccionService;

    //Este Repository se utiliza temporalmente para buscar al usuario mediante su ID
    private final UsuarioRepository usuarioRepository;

    //Regisrar una nueva transacción, por ahora: El usuarioId viene en la URL.
    @Operation(summary = "Registra una nueva transacción para un usuario específico")
    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<TransaccionDetails> registrar(
            @PathVariable Long usuarioId,

            @Valid
            @RequestBody
            TransaccionRegister datos
    ) {
        //TEMPORAL PARA LAS PRUEBAS
        Usuario usuario =
                usuarioRepository
                        .findById(usuarioId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Usuario no encontrado"
                                )
                        );

        TransaccionDetails respuesta =
                transaccionService
                        .registrarTransaccion(
                                datos,
                                usuario
                        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    @Operation(summary = "Obtiene todas las transacciones de un usuario específico")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<TransaccionDetails>>
    obtenerTransacciones(
            @PathVariable Long usuarioId
    ) {
        List<TransaccionDetails> transacciones =
                transaccionService
                        .obtenerTransacciones(
                                usuarioId
                        );

        return ResponseEntity.ok(
                transacciones
        );
    }

    @Operation(summary = "Obtiene todas las transacciones de un usuario específico filtradas por tipo")
    @GetMapping("/usuario/{usuarioId}/tipo")
    public ResponseEntity<List<TransaccionDetails>>
    obtenerPorTipo(
            @PathVariable Long usuarioId,
            @RequestParam
            TipoTransaccion tipo
    ) {

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
    @GetMapping("/usuario/{usuarioId}/sin-clasificar")
    public ResponseEntity<List<TransaccionDetails>>
    obtenerSinClasificar(
            @PathVariable Long usuarioId
    ) {
        List<TransaccionDetails> transacciones =
                transaccionService
                        .obtenerTransaccionesSinClasificar(
                                usuarioId
                        );
        return ResponseEntity.ok(
                transacciones
        );
    }
}