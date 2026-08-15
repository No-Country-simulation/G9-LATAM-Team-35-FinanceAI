package com.team35.backend.dto;

import com.team35.backend.enums.TipoTransaccion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransaccionRegister {

    @NotBlank(message = "La descripción de la transacción no puede estar vacía")
    private String descripcion;

    @NotNull(message = "El valor de la transacción es obligatorio")
    @Positive(message = "El valor de la transacción debe ser mayor que cero")
    private BigDecimal valor;

    //Indica si se trata de un ingreso o un gasto.
    @NotNull(message = "El tipo de transacción es obligatorio")
    private TipoTransaccion tipo;
    @NotNull(message = "La fecha de la transacción es obligatoria")
    private LocalDate fecha;

    private String categoriaNombre; // Nombre de la categoría asociada a la trans

    public TransaccionRegister() {
    }

    public TransaccionRegister(
            String descripcion,
            BigDecimal valor,
            TipoTransaccion tipo,
            LocalDate fecha,
            String categoriaNombre
    ) {
        this.descripcion = descripcion;
        this.valor = valor;
        this.tipo = tipo;
        this.fecha = fecha;
        //si categoriaNombre es null, se asigna null, de lo contrario se asigna el valor proporcionado
        this.categoriaNombre = categoriaNombre != null ? categoriaNombre : null;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransaccion tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }
}