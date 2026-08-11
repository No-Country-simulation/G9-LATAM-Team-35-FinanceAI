package com.team35.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errores = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage(),
                        (existente, nuevo) -> existente
                ));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Datos de entrada inválidos");
        body.put("detalles", errores);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(HttpMessageNotReadableException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "El cuerpo de la solicitud no tiene un formato JSON válido");

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(com.team35.backend.service.UsuarioService.UsuarioNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleUsuarioNoEncontrado(
            com.team35.backend.service.UsuarioService.UsuarioNoEncontradoException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Usuario no encontrado");
        body.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(com.team35.backend.service.UsuarioService.MonedaInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleMonedaInvalida(
            com.team35.backend.service.UsuarioService.MonedaInvalidaException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Moneda inválida");
        body.put("mensaje", ex.getMessage());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Ocurrió un error interno al procesar la solicitud");
        body.put("mensaje", ex.getMessage());

        return ResponseEntity.internalServerError().body(body);
    }

    // --- Métodos usados directamente por SecurityConfig (no son @ExceptionHandler
    // porque Spring Security maneja estos casos ANTES de llegar al DispatcherServlet,
    // así que no pasan por el mecanismo normal de @RestControllerAdvice) ---

    /**
     * Se llama cuando no hay token, el token es inválido/expiró, o falta el header
     * Authorization en un endpoint protegido. Responde 401.
     */
    public void handleAuthenticationError(HttpServletRequest request, HttpServletResponse response,
                                           AuthenticationException authException) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "No autenticado");
        body.put("mensaje", "Se requiere un token válido para acceder a este recurso");

        escribirRespuesta(response, HttpStatus.UNAUTHORIZED, body);
    }

    /**
     * Se llama cuando el token es válido pero el usuario no tiene permiso para
     * el recurso solicitado. Responde 403.
     */
    public void handleAccessDenied(HttpServletRequest request, HttpServletResponse response,
                                    AccessDeniedException accessDeniedException) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Acceso denegado");
        body.put("mensaje", "No tienes permisos para acceder a este recurso");

        escribirRespuesta(response, HttpStatus.FORBIDDEN, body);
    }

    private void escribirRespuesta(HttpServletResponse response, HttpStatus status, Map<String, Object> body) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
