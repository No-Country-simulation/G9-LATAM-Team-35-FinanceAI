package com.team35.backend.dto;

public class ClasificacionTransaccionResponse {

    private String descripcion;
    private double valor;
    private String categoria;
    private double confianza;

    public ClasificacionTransaccionResponse() {
    }

    public ClasificacionTransaccionResponse(String descripcion, double valor, String categoria, double confianza) {
        this.descripcion = descripcion;
        this.valor = valor;
        this.categoria = categoria;
        this.confianza = confianza;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getConfianza() {
        return confianza;
    }

    public void setConfianza(double confianza) {
        this.confianza = confianza;
    }
}
