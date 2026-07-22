package com.team35.backend.service;

import com.team35.backend.dto.AnalisisFinancieroRequest;
import com.team35.backend.dto.AnalisisFinancieroResponse;
import com.team35.backend.dto.TransaccionInputDTO;
import com.team35.backend.util.PerfilTextoMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalisisFinancieroService {

    private final ClasificadorTransaccionesService clasificadorService;
    private final PerfilFinancieroService perfilService;
    private final RecomendacionService recomendacionService;

    public AnalisisFinancieroService(ClasificadorTransaccionesService clasificadorService,
                                      PerfilFinancieroService perfilService,
                                      RecomendacionService recomendacionService) {
        this.clasificadorService = clasificadorService;
        this.perfilService = perfilService;
        this.recomendacionService = recomendacionService;
    }

    public AnalisisFinancieroResponse analizar(AnalisisFinancieroRequest request) {

        // 1. Clasificar cada transacción y sumar por categoría.
        // Las keys del resumen van en minúsculas para coincidir con el contrato
        // acordado con Data Science (ej. "alimentacion", no "ALIMENTACION").
        Map<String, Double> resumenGastos = new LinkedHashMap<>();
        double totalGastos = 0.0;

        for (TransaccionInputDTO transaccion : request.getTransacciones()) {
            String categoria = clasificadorService.inferirCategoria(transaccion.getDescripcion());
            resumenGastos.merge(categoria.toLowerCase(), transaccion.getValor(), Double::sum);
            totalGastos += transaccion.getValor();
        }

        // 2. Evaluar perfil financiero (funciona igual para 1 o N transacciones)
        PerfilFinancieroService.ResultadoPerfil resultadoPerfil = perfilService.evaluar(
                request.getIngresoMensual(),
                request.getNivelEndeudamiento(),
                request.getFrecuenciaAhorro(),
                totalGastos
        );

        // 3. Generar recomendaciones
        List<String> recomendaciones = recomendacionService.generar(
                resultadoPerfil.perfil,
                request.getFrecuenciaAhorro(),
                resumenGastos
        );

        // 4. Traducir el enum interno (EN_OBSERVACION) al texto legible que
        // espera el contrato de la API ("En observación")
        return new AnalisisFinancieroResponse(
                PerfilTextoMapper.aTexto(resultadoPerfil.perfil),
                resultadoPerfil.probabilidad,
                resumenGastos,
                recomendaciones
        );
    }
}
