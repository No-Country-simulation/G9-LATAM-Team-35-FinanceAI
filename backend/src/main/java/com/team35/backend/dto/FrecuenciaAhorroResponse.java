package com.team35.backend.dto;

/**
 * Respuesta del cálculo de frecuencia de ahorro, ya sea por encuesta o cálculo automático.
 */
public class FrecuenciaAhorroResponse {

    /** ENCUESTA o AUTOMATICO */
    private String metodo;

    /** Puntaje calculado (solo para encuesta) */
    private Double puntos;

    /** Escala máxima (solo para encuesta, ej. 4.0) */
    private Double escalaMaxima;

    /** Resultado final: BAJA, MEDIA, ALTA */
    private String frecuenciaAhorro;

    /** Cantidad de meses con datos (solo para método AUTOMATICO) */
    private Integer mesesConDatos;

    /** Mensaje explicativo para mostrar al usuario */
    private String mensaje;

    public FrecuenciaAhorroResponse() {}

    // Constructor para encuesta
    public FrecuenciaAhorroResponse(String metodo, double puntos, double escalaMaxima, String frecuenciaAhorro) {
        this.metodo = metodo;
        this.puntos = puntos;
        this.escalaMaxima = escalaMaxima;
        this.frecuenciaAhorro = frecuenciaAhorro;
        this.mensaje = generarMensaje(frecuenciaAhorro, metodo);
    }

    // Constructor para cálculo automático
    public FrecuenciaAhorroResponse(String metodo, String frecuenciaAhorro, int mesesConDatos) {
        this.metodo = metodo;
        this.frecuenciaAhorro = frecuenciaAhorro;
        this.mesesConDatos = mesesConDatos;
        this.mensaje = generarMensaje(frecuenciaAhorro, metodo);
    }

    private String generarMensaje(String frecuencia, String metodo) {
        boolean esEncuesta = "ENCUESTA".equals(metodo);
        String sufijo = esEncuesta
                ? " (valor preliminar, se actualizará con tu historial)"
                : " (calculado automáticamente con tu historial)";
        return switch (frecuencia.toUpperCase()) {
            case "ALTA" -> "Frecuencia de ahorro Alta" + sufijo;
            case "MEDIA" -> "Frecuencia de ahorro Media" + sufijo;
            default -> "Frecuencia de ahorro Baja" + sufijo;
        };
    }

    // Getters y Setters
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }

    public Double getPuntos() { return puntos; }
    public void setPuntos(Double puntos) { this.puntos = puntos; }

    public Double getEscalaMaxima() { return escalaMaxima; }
    public void setEscalaMaxima(Double escalaMaxima) { this.escalaMaxima = escalaMaxima; }

    public String getFrecuenciaAhorro() { return frecuenciaAhorro; }
    public void setFrecuenciaAhorro(String frecuenciaAhorro) { this.frecuenciaAhorro = frecuenciaAhorro; }

    public Integer getMesesConDatos() { return mesesConDatos; }
    public void setMesesConDatos(Integer mesesConDatos) { this.mesesConDatos = mesesConDatos; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
