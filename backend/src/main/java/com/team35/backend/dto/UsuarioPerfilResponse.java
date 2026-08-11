package com.team35.backend.dto;

public class UsuarioPerfilResponse {

    private Long id;
    private String nombre;
    private String email;
    private String moneda;       // código, ej. "MXN"
    private String monedaTexto;  // legible, ej. "Peso Mexicano"

    public UsuarioPerfilResponse() {
    }

    public UsuarioPerfilResponse(Long id, String nombre, String email, String moneda, String monedaTexto) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.moneda = moneda;
        this.monedaTexto = monedaTexto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getMonedaTexto() {
        return monedaTexto;
    }

    public void setMonedaTexto(String monedaTexto) {
        this.monedaTexto = monedaTexto;
    }
}
