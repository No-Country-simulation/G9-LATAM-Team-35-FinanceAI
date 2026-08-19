package com.team35.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaDTO {

    @JsonAlias({"tipo", "Tipo"})
    private String tipo;

    @JsonAlias({"nivel", "Nivel"})
    private String nivel;

    @JsonAlias({"mensaje", "Mensaje"})
    private String mensaje;

    private String accion;

    private String categoria;
}
