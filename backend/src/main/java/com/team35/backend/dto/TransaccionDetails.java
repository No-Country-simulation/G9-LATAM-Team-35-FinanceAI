package com.team35.backend.dto;

import com.team35.backend.enums.TipoTransaccion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransaccionDetails {

    private Long id;
    private String descripcion;
    private BigDecimal valor;
    // Tipo de movimiento: INGRESO o GASTO
    private TipoTransaccion tipo;
    private String categoria;
    //Fecha en la que ocurrió la transacción.
    private LocalDate fecha;
    //Fecha en la que se registró la transacción en el sistema
    private LocalDateTime creadoEn;

    public TransaccionDetails() {
    }

    public TransaccionDetails(
            Long id,
            String descripcion,
            BigDecimal valor,
            TipoTransaccion tipo,
            String categoria,
            LocalDate fecha,
            LocalDateTime creadoEn
    ) {
        this.id = id;
        this.descripcion = descripcion;
        this.valor = valor;
        this.tipo = tipo;
        this.categoria = categoria;
        this.fecha = fecha;
        this.creadoEn = creadoEn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}