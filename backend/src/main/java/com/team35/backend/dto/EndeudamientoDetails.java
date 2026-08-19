package com.team35.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class EndeudamientoDetails {

    private BigDecimal nivelEndeudamiento;
    private BigDecimal totalDeudas;
    private BigDecimal ingresoMensual;
    private String mensaje;


    public static EndeudamientoDetails from(
            BigDecimal nivelEndeudamiento,
            BigDecimal totalDeudas,
            BigDecimal ingresoMensual,
            String mensaje
    ) {
        return new EndeudamientoDetails(
                nivelEndeudamiento,
                totalDeudas,
                ingresoMensual,
                mensaje
        );
    }
}