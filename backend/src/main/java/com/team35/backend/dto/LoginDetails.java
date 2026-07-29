package com.team35.backend.dto;

//DTO utilizado como respuesta al iniciar sesión, contiene el token JWT y la información del usuario.

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginDetails {

    private String token;
    private UsuarioDetails usuario;

}
