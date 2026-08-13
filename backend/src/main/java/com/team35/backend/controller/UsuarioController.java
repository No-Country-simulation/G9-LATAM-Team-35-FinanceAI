package com.team35.backend.controller;

import com.team35.backend.dto.ActualizarUsuarioRequest;
import com.team35.backend.dto.MonedaDisponibleDTO;
import com.team35.backend.dto.UsuarioPerfilResponse;
import com.team35.backend.service.AuthService;
import com.team35.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Configuración", description = "Perfil del usuario y preferencias (moneda) para la página de Configuración")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    public UsuarioController(UsuarioService usuarioService, AuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
    }

    @Operation(summary = "Obtiene el perfil del usuario autenticado para precargar la página de Configuración")
    @GetMapping("/me")
    public ResponseEntity<UsuarioPerfilResponse> obtenerPerfil() {
        Long usuarioId = authService.getUsuarioIdAutenticado();
        return ResponseEntity.ok(usuarioService.obtenerPerfil(usuarioId));
    }

    @Operation(summary = "Actualiza nombre y/o moneda del usuario autenticado")
    @PutMapping("/me")
    public ResponseEntity<UsuarioPerfilResponse> actualizarPerfil(
            @Valid @RequestBody ActualizarUsuarioRequest request) {
        Long usuarioId = authService.getUsuarioIdAutenticado();
        return ResponseEntity.ok(usuarioService.actualizarPerfil(usuarioId, request));
    }

    @Operation(summary = "Lista las monedas disponibles para el selector de la página de Configuración")
    @GetMapping("/monedas-disponibles")
    public ResponseEntity<List<MonedaDisponibleDTO>> listarMonedas() {
        return ResponseEntity.ok(usuarioService.listarMonedasDisponibles());
    }
}
