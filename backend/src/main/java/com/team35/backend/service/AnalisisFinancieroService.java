package com.team35.backend.service;

import com.team35.backend.dto.AnalisisFinancieroRequest;
import com.team35.backend.dto.AnalisisFinancieroResponse;
import com.team35.backend.dto.TransaccionInputDTO;
import com.team35.backend.entity.Analisis;
import com.team35.backend.entity.Recomendacion;
import com.team35.backend.entity.Usuario;
import com.team35.backend.enums.PerfilTipo;
import com.team35.backend.repository.AnalisisRepository;
import com.team35.backend.repository.UsuarioRepository;
import com.team35.backend.util.PerfilTextoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AnalisisFinancieroService {

    private final ClasificadorTransaccionesService clasificadorService;
    private final PerfilFinancieroService perfilService;
    private final RecomendacionService recomendacionService;
    private final AnalisisRepository analisisRepository;
    private final UsuarioRepository usuarioRepository;

    public AnalisisFinancieroService(ClasificadorTransaccionesService clasificadorService,
                                     PerfilFinancieroService perfilService,
                                     RecomendacionService recomendacionService,
                                     AnalisisRepository analisisRepository,
                                     UsuarioRepository usuarioRepository) {
        this.clasificadorService = clasificadorService;
        this.perfilService = perfilService;
        this.recomendacionService = recomendacionService;
        this.analisisRepository = analisisRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Realiza el análisis financiero y, si se proporciona un usuarioId, persiste el resultado en BD.
     *
     * @param request   Datos del análisis (ingresos, endeudamiento, frecuencia, transacciones)
     * @param usuarioId ID del usuario autenticado, o null si es invitado (no se guarda)
     * @return Resultado del análisis con perfil, probabilidad, resumen y recomendaciones
     */
    @Transactional
    public AnalisisFinancieroResponse analizar(AnalisisFinancieroRequest request, Long usuarioId) {

        // 1. Clasificar cada transacción y sumar por categoría.
        // Las keys del resumen van en minúsculas para coincidir con el contrato
        // acordado con Data Science (ej. "alimentacion", no "ALIMENTACION").
        Map<String, Double> resumenGastos = new LinkedHashMap<>();
        double totalGastos = 0.0;

        for (TransaccionInputDTO transaccion : request.getTransacciones()) {
            String categoria = clasificadorService.clasificar(transaccion).getCategoria_gasto();
            resumenGastos.merge(categoria.toLowerCase(), transaccion.getValor(), Double::sum);
            totalGastos += transaccion.getValor();
        }

        // 2. Evaluar perfil financiero (con fallback al mock si FastAPI no está disponible)
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

        // 4. Persistir el análisis en BD (solo para usuarios autenticados)
        if (usuarioId != null) {
            persistirAnalisis(request, resultadoPerfil, recomendaciones, usuarioId);
        }

        // 5. Traducir el enum interno (EN_OBSERVACION) al texto legible que
        // espera el contrato de la API ("En observación")
        return new AnalisisFinancieroResponse(
                PerfilTextoMapper.aTexto(resultadoPerfil.perfil),
                resultadoPerfil.probabilidad,
                resumenGastos,
                recomendaciones
        );
    }

    /**
     * Sobrecarga para compatibilidad: análisis sin persistencia (invitados).
     */
    public AnalisisFinancieroResponse analizar(AnalisisFinancieroRequest request) {
        return analizar(request, null);
    }

    // ─────────────────────────────────────────────────────────────
    //  Persistencia interna
    // ─────────────────────────────────────────────────────────────

    private void persistirAnalisis(AnalisisFinancieroRequest request,
                                   PerfilFinancieroService.ResultadoPerfil resultadoPerfil,
                                   List<String> textoRecomendaciones,
                                   Long usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            // El usuario no existe en BD — no persistir silenciosamente
            return;
        }

        Analisis analisis = Analisis.builder()
                .usuario(usuarioOpt.get())
                .perfil(resultadoPerfil.perfil)
                .probabilidad(BigDecimal.valueOf(resultadoPerfil.probabilidad))
                .ingresoMensual(BigDecimal.valueOf(request.getIngresoMensual()))
                .nivelEndeudamiento(BigDecimal.valueOf(request.getNivelEndeudamiento()))
                .frecuenciaAhorro(request.getFrecuenciaAhorro())
                .fechaAnalisis(LocalDateTime.now())
                .build();

        // Agregar las recomendaciones al análisis
        for (String texto : textoRecomendaciones) {
            Recomendacion rec = Recomendacion.builder()
                    .texto(texto)
                    .build();
            analisis.agregarRecomendacion(rec);
        }

        analisisRepository.save(analisis);
    }
}
