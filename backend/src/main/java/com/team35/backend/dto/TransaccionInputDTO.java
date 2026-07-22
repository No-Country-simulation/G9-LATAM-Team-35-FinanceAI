package com.team35.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class TransaccionInputDTO {

    @NotBlank(message = "La descripción de la transacción no puede estar vacía")
    private String descripcion;

    @Positive(message = "El valor de la transacción debe ser mayor que cero")
    private double valor;

    public TransaccionInputDTO() {
    }

    public TransaccionInputDTO(String descripcion, double valor) {
        this.descripcion = descripcion;
        this.valor = valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
}
