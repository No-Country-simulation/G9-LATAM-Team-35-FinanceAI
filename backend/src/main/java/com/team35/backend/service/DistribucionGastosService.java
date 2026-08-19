package com.team35.backend.service;

import com.team35.backend.dto.DistribucionCategoriaDTO;
import com.team35.backend.dto.DistribucionGastosResponse;
import com.team35.backend.entity.Transaccion;
import com.team35.backend.enums.TipoTransaccion;
import com.team35.backend.repository.TransaccionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Trae la distribución de gastos por categoría de un usuario, para un mes dado,
 * usando SOLO datos que ya están clasificados en BD. No corre ningún modelo de IA
 * aquí — esa parte ya pasó cuando se clasificó/analizó la transacción.
 *
 * Formato de respuesta acordado en Notion (Página 2, Opción A): lista de
 * {categoria, total, porcentaje}, ordenada de mayor a menor gasto.
 */
@Service
public class DistribucionGastosService {

    private final TransaccionRepository transaccionRepository;

    public DistribucionGastosService(TransaccionRepository transaccionRepository) {
        this.transaccionRepository = transaccionRepository;
    }

    /**
     * @return null si no hay ninguna transacción clasificada ese mes (el frontend
     * decide qué mensaje mostrar cuando recibe null — ver nota en Notion).
     */
    public DistribucionGastosResponse obtenerDistribucion(Long usuarioId, int mes, int anio) {
        YearMonth periodo = YearMonth.of(anio, mes);
        LocalDate desde = periodo.atDay(1);
        LocalDate hasta = periodo.atEndOfMonth();

        List<Transaccion> gastos = transaccionRepository
                .findByUsuarioIdAndTipoAndCategoriaIsNotNullAndFechaBetween(
                        usuarioId, TipoTransaccion.GASTO, desde, hasta);

        if (gastos.isEmpty()) {
            return null;
        }

        Map<String, Double> totalesPorCategoria = new LinkedHashMap<>();
        for (Transaccion transaccion : gastos) {
            String categoria = transaccion.getCategoria().getNombre(); // ya viene en mayúsculas (ALIMENTACION, etc.)
            totalesPorCategoria.merge(categoria, transaccion.getValor().doubleValue(), Double::sum);
        }

        double totalGeneral = totalesPorCategoria.values().stream().mapToDouble(Double::doubleValue).sum();

        List<DistribucionCategoriaDTO> distribucion = totalesPorCategoria.entrySet().stream()
                .map(e -> new DistribucionCategoriaDTO(
                        e.getKey(),
                        e.getValue(),
                        totalGeneral > 0 ? (e.getValue() / totalGeneral) * 100 : 0
                ))
                .sorted((a, b) -> Double.compare(b.getTotal(), a.getTotal())) // mayor a menor
                .collect(Collectors.toList());

        return new DistribucionGastosResponse(distribucion);
    }
}
