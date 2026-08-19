package com.team35.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class TransaccionInputDTO {

    @NotBlank(message = "La descripción de la transacción no puede estar vacía")
    private String descripcion;

    @Min(value = 0, message = "El valor de la transacción no puede ser negativo")
    @JsonAlias({"valor", "monto"})
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
