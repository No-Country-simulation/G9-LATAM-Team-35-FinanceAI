package com.team35.backend.dto;

/**
 * DTO que representa exactamente la respuesta enviada por
 * el servicio de Ciencia de Datos.
 *
 * Backend simplemente recibe este objeto y lo devuelve al frontend.
 */
public class ClasificacionTransaccionResponse {

    private String descripcion;
    private double valor;
    private String categoria_gasto;


    public ClasificacionTransaccionResponse() {
    }

    public ClasificacionTransaccionResponse(String descripcion, double valor, String categoria_gasto) {
        this.descripcion = descripcion;
        this.valor = valor;
        this.categoria_gasto = categoria_gasto;
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

    public String getCategoria_gasto() {
        return categoria_gasto;
    }

    public void setCategoria_gasto(String categoria_gasto) {
        this.categoria_gasto = categoria_gasto;
    }
}
