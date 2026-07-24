package com.team35.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO utilizado para recibir los datos necesarios
 * para registrar un nuevo usuario.
 * Este DTO representa los datos que enviará el Frontend.
 */
@Getter
@Setter
public class UsuarioRegister {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(
            min = 8,
            message = "La contraseña debe tener al menos 8 caracteres"
    )
    private String password;
}