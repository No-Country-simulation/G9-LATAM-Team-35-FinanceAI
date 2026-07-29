package com.team35.backend.controller;

import com.team35.backend.dto.LoginDetails;
import com.team35.backend.dto.UsuarioDetails;
import com.team35.backend.dto.UsuarioLogin;
import com.team35.backend.dto.UsuarioRegister;
import com.team35.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

/**
 * Controller encargado de los endpoints
 * relacionados con autenticación y usuarios.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @Operation(summary = "Registra un nuevo usuario en el sistema")
    @PostMapping("/register")
    public ResponseEntity<UsuarioDetails> registrar(
            @Valid @RequestBody UsuarioRegister datos
    ) {

        UsuarioDetails usuario =
                authService.registrar(datos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuario);
    }

    //inicia sesión
    @PostMapping("/login")
    public ResponseEntity<LoginDetails> login(
            @Valid @RequestBody UsuarioLogin datos
    ) {
        LoginDetails respuesta =
                authService.login(datos);
        return ResponseEntity.ok(respuesta);
    }

}