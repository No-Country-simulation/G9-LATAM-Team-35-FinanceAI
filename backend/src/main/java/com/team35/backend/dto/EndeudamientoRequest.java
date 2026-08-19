package com.team35.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndeudamientoRequest {
    @NotNull(message = "El ingreso mensual es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El ingreso mensual debe ser mayor a 0")
    @JsonProperty("ingresoMensual")
    private BigDecimal ingresoMensual;

    @NotEmpty(message = "Debes agregar al menos una deuda")
    @JsonProperty("cuotasMensuales")
    private List<BigDecimal> cuotasMensuales;
}