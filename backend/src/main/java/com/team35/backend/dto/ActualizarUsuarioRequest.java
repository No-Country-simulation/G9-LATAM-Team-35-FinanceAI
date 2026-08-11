package com.team35.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class ActualizarUsuarioRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    // Se valida contra el enum Moneda en el service (mensaje de error más claro
    // que dejarlo fallar como 400 genérico de deserialización).
    @NotBlank(message = "La moneda es obligatoria")
    private String moneda;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }
}
