package com.team35.backend.service;

import com.team35.backend.dto.AnalisisDetails;
import com.team35.backend.dto.EndeudamientoRequest;
import com.team35.backend.dto.EndeudamientoDetails;
import com.team35.backend.entity.Analisis;
import com.team35.backend.entity.Recomendacion;
import com.team35.backend.enums.PerfilTipo;
import com.team35.backend.repository.AnalisisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalisisService {

    private final AnalisisRepository analisisRepository;

    public List<AnalisisDetails> obtenerHistorial(Long usuarioId) {
        List<Analisis> analisis =
                analisisRepository.findByUsuarioIdWithRecomendaciones(usuarioId);

        // Convertir a DTOs usando el Mapper
        return analisis.stream()
                .map(this::toBusquedaDTO)
                .collect(Collectors.toList());
            }

    public List<AnalisisDetails> buscarAnalisis(Long usuarioId, String query) {
        // Obtener todos los análisis con recomendaciones
        List<Analisis> analisis = analisisRepository
                .findByUsuarioIdWithRecomendaciones(usuarioId);

        // Convertir a DTOs (el nombre se genera en español)
        List<AnalisisDetails> todos = analisis.stream()
                .map(this::toBusquedaDTO)
                .collect(Collectors.toList());

        // 3. Si no hay query, devolver todos
        if (query == null || query.trim().isEmpty()) {
            return todos;
        }

        //  Filtrar en memoria (case-insensitive)
        String q = query.toLowerCase().trim();
        return todos.stream()
                .filter(dto -> {
                    String nombre = dto.getNombre() != null ? dto.getNombre().toLowerCase() : "";
                    String perfil = dto.getPerfil() != null ? dto.getPerfil().toString().toLowerCase() : "";

                    // Buscar en el nombre generado (ej: "Agosto 2026 - SALUDABLE")
                    return nombre.contains(q) || perfil.contains(q);
                })
                .collect(Collectors.toList());
    }

    // En AnalisisService
    private AnalisisDetails toBusquedaDTO(Analisis analisis) {
        if (analisis == null) return null;
        List<String> recomendacionesTextos = analisis.getRecomendaciones() != null
                ? analisis.getRecomendaciones().stream()
                .map(Recomendacion::getTexto)
                .collect(Collectors.toList())
                : List.of();

        return new AnalisisDetails(
                analisis.getId(),
                analisis.getPerfil(),
                analisis.getProbabilidad(),
                analisis.getIngresoMensual(),
                analisis.getNivelEndeudamiento(),
                analisis.getFrecuenciaAhorro(),
                analisis.getFechaAnalisis(),
                recomendacionesTextos
        );
    }


    public EndeudamientoDetails calcularEndeudamiento(EndeudamientoRequest request) {
        // Sumar todas las cuotas de deuda (directamente de la lista)
        BigDecimal totalDeudas = request.getCuotasMensuales().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Validar ingreso
        BigDecimal ingreso = request.getIngresoMensual();
        if (ingreso.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El ingreso mensual debe ser mayor a 0");
        }

        // Calcular nivel de endeudamiento
        BigDecimal nivelEndeudamiento = totalDeudas
                .divide(ingreso, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);

        // Generar mensaje según nivel de endeudamiento
        String mensaje;
        if (nivelEndeudamiento.compareTo(new BigDecimal("30")) < 0) {
            mensaje = "Tu nivel de endeudamiento es saludable.";
        } else if (nivelEndeudamiento.compareTo(new BigDecimal("50")) < 0) {
            mensaje = "Tu nivel de endeudamiento es moderado. Considera reducir tus deudas.";
        } else {
            mensaje = "Tu nivel de endeudamiento es alto. Es recomendable que busques asesoría financiera para mejorar tu situación.";
        }

        return EndeudamientoDetails.from(
                nivelEndeudamiento,
                totalDeudas,
                ingreso,
                mensaje
        );
    }
}