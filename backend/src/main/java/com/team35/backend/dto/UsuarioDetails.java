package com.team35.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO utilizado para devolver la información pública
 * del usuario después de registrarlo correctamente.
 */
@Getter
@AllArgsConstructor
public class UsuarioDetails {

    private Long id;
    private String nombre;
    private String email;
}