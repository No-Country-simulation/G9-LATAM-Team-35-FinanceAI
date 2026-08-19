package com.team35.backend.service;

import com.team35.backend.entity.Transaccion;
import com.team35.backend.enums.TipoTransaccion;
import com.team35.backend.repository.TransaccionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Trae la distribución de gastos por categoría de un usuario, para un mes dado,
 * usando SOLO datos que ya están clasificados en BD. No corre ningún modelo de IA
 * aquí — esa parte ya pasó cuando se clasificó/analizó la transacción.
 *
 * Pensado para que Lucero (frontend) arme la gráfica de distribución.
 */
@Service
public class DistribucionGastosService {

    private final TransaccionRepository transaccionRepository;

    public DistribucionGastosService(TransaccionRepository transaccionRepository) {
        this.transaccionRepository = transaccionRepository;
    }

    /**
     * @return mapa categoria(lowercase) -> total gastado en el mes, o null si no hay
     * ninguna transacción clasificada ese mes (el frontend decide qué mensaje mostrar
     * cuando recibe null — ver nota en Notion).
     */
    public Map<String, Double> obtenerDistribucion(Long usuarioId, YearMonth mes) {
        LocalDate desde = mes.atDay(1);
        LocalDate hasta = mes.atEndOfMonth();

        List<Transaccion> gastos = transaccionRepository
                .findByUsuarioIdAndTipoAndCategoriaIsNotNullAndFechaBetween(
                        usuarioId, TipoTransaccion.GASTO, desde, hasta);

        if (gastos.isEmpty()) {
            return null;
        }

        Map<String, Double> distribucion = new LinkedHashMap<>();
        for (Transaccion transaccion : gastos) {
            String categoria = transaccion.getCategoria().getNombre().toLowerCase();
            distribucion.merge(categoria, transaccion.getValor().doubleValue(), Double::sum);
        }
        return distribucion;
    }

    /**
     * Sobrecarga conveniente para recibir el período como String "YYYY-MM".
     * Devuelve un mapa vacío si no hay datos (en vez de null) para facilitar
     * el uso en AlertaService.
     */
    public Map<String, Double> obtenerPorPeriodo(Long usuarioId, String mesAnio) {
        try {
            YearMonth ym = YearMonth.parse(mesAnio);
            Map<String, Double> result = obtenerDistribucion(usuarioId, ym);
            return result != null ? result : new LinkedHashMap<>();
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }
}
