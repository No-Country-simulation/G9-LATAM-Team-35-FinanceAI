package com.team35.backend.dto;

public class ClasificacionTransaccionResponse {

    private String descripcion;
    private double valor;

    // OJO: Python devuelve "categoria_gasto" en el JSON, no "categoria".
    // Con snake_case activo, Jackson mapea este campo Java a esa key exacta,
    // así que el nombre del campo Java debe coincidir en snake_case invertido.
    private String categoriaGasto;

    public ClasificacionTransaccionResponse() {
    }

    public ClasificacionTransaccionResponse(String descripcion, double valor, String categoriaGasto) {
        this.descripcion = descripcion;
        this.valor = valor;
        this.categoriaGasto = categoriaGasto;
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

    public String getCategoriaGasto() {
        return categoriaGasto;
    }

    public void setCategoriaGasto(String categoriaGasto) {
        this.categoriaGasto = categoriaGasto;
    }
}
