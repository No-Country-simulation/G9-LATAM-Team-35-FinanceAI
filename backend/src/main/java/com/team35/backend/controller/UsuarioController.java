package com.team35.backend.controller;

import com.team35.backend.dto.ActualizarUsuarioRequest;
import com.team35.backend.dto.CambiarContrasenaRequest;
import com.team35.backend.dto.MonedaDisponibleDTO;
import com.team35.backend.dto.UsuarioPerfilResponse;
import com.team35.backend.service.AuthService;
import com.team35.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Usuarios", description = "Endpoints relacionados con el perfil del usuario y sus preferencias")
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

    @Operation(summary = "Cambia la contraseña del usuario autenticado")
    @PutMapping("/me/cambiar-contrasena")
    public ResponseEntity<Map<String, String>> cambiarContrasena(
            @Valid @RequestBody CambiarContrasenaRequest request) {
        Long usuarioId = authService.getUsuarioIdAutenticado();
        usuarioService.cambiarContrasena(usuarioId, request);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Contraseña actualizada correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lista las monedas disponibles para el selector de la página de Configuración")
    @GetMapping("/monedas-disponibles")
    public ResponseEntity<List<MonedaDisponibleDTO>> listarMonedas() {
        return ResponseEntity.ok(usuarioService.listarMonedasDisponibles());
    }
}
