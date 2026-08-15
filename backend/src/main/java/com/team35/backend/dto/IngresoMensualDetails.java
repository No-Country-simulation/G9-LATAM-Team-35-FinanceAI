package com.team35.backend.dto;

import java.math.BigDecimal;

public class IngresoMensualDetails {

    private BigDecimal ingreso_mensual;
    private boolean tiene_datos;
    private String mensaje;

    public IngresoMensualDetails() {
    }

    public IngresoMensualDetails(
            BigDecimal ingreso_mensual,
            boolean tiene_datos,
            String mensaje
    ) {
        this.ingreso_mensual = ingreso_mensual;
        this.tiene_datos = tiene_datos;
        this.mensaje = mensaje;
    }

    public BigDecimal getIngreso_mensual() {
        return ingreso_mensual;
    }

    public void setIngreso_mensual(BigDecimal ingreso_mensual) {
        this.ingreso_mensual = ingreso_mensual;
    }

    public boolean isTiene_datos() {
        return tiene_datos;
    }

    public void setTiene_datos(boolean tiene_datos) {
        this.tiene_datos = tiene_datos;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}