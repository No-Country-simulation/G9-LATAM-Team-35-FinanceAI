package com.team35.backend.dto;

public class DistribucionCategoriaDTO {

    private String categoria;
    private double total;
    private double porcentaje;

    public DistribucionCategoriaDTO() {
    }

    public DistribucionCategoriaDTO(String categoria, double total, double porcentaje) {
        this.categoria = categoria;
        this.total = total;
        this.porcentaje = porcentaje;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(double porcentaje) {
        this.porcentaje = porcentaje;
    }
}
