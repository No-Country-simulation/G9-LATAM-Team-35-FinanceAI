package com.team35.backend.controller;

import com.team35.backend.dto.ActualizarUsuarioRequest;
import com.team35.backend.dto.MonedaDisponibleDTO;
import com.team35.backend.dto.UsuarioPerfilResponse;
import com.team35.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Configuración", description = "Perfil del usuario y preferencias (moneda) para la página de Configuración")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // TODO: reemplazar {id} por el usuario autenticado (JWT) cuando esté listo,
    // usando algo como GET/PUT /usuarios/me en vez de pedir el id explícito.

    @Operation(summary = "Obtiene el perfil del usuario para precargar la página de Configuración")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioPerfilResponse> obtenerPerfil(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(id));
    }

    @Operation(summary = "Actualiza nombre y/o moneda del usuario")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioPerfilResponse> actualizarPerfil(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(id, request));
    }

    @Operation(summary = "Lista las monedas disponibles para el selector de la página de Configuración")
    @GetMapping("/monedas-disponibles")
    public ResponseEntity<List<MonedaDisponibleDTO>> listarMonedas() {
        return ResponseEntity.ok(usuarioService.listarMonedasDisponibles());
    }
}
