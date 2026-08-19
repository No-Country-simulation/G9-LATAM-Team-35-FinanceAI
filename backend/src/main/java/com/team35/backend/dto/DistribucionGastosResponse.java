package com.team35.backend.dto;

import java.util.List;

public class DistribucionGastosResponse {

    private List<DistribucionCategoriaDTO> distribucion;

    public DistribucionGastosResponse() {
    }

    public DistribucionGastosResponse(List<DistribucionCategoriaDTO> distribucion) {
        this.distribucion = distribucion;
    }

    public List<DistribucionCategoriaDTO> getDistribucion() {
        return distribucion;
    }

    public void setDistribucion(List<DistribucionCategoriaDTO> distribucion) {
        this.distribucion = distribucion;
    }
}
