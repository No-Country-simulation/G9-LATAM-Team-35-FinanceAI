package com.team35.backend.dto;

public class MonedaDisponibleDTO {

    private String codigo; // "MXN"
    private String texto;  // "Peso Mexicano"

    public MonedaDisponibleDTO() {
    }

    public MonedaDisponibleDTO(String codigo, String texto) {
        this.codigo = codigo;
        this.texto = texto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
