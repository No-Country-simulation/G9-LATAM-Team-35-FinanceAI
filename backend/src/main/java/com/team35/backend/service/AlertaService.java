package com.team35.backend.service;

import com.team35.backend.dto.AlertaDTO;
import com.team35.backend.entity.Analisis;
import com.team35.backend.entity.Transaccion;
import com.team35.backend.enums.TipoTransaccion;
import com.team35.backend.repository.AnalisisRepository;
import com.team35.backend.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AlertaService {

    private final TransaccionRepository transaccionRepository;
    private final AnalisisRepository analisisRepository;
    private final DistribucionGastosService distribucionGastosService;

    public List<AlertaDTO> generarAlertasUsuario(Long usuarioId, String mesAnio) {
        List<AlertaDTO> alertas = new ArrayList<>();

        // Determinar rango de fechas para el mes
        YearMonth ym;
        try {
            if (mesAnio != null && mesAnio.matches("\\d{4}-\\d{2}")) {
                ym = YearMonth.parse(mesAnio);
            } else {
                ym = YearMonth.now();
            }
        } catch (Exception e) {
            ym = YearMonth.now();
        }

        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        // 1. Obtener Ingreso y Gasto Total
        BigDecimal totalIngresoBD = transaccionRepository.calcularTotalPorTipoYPeriodo(
                usuarioId, TipoTransaccion.INGRESO, inicio, fin);
        BigDecimal totalGastoBD = transaccionRepository.calcularTotalPorTipoYPeriodo(
                usuarioId, TipoTransaccion.GASTO, inicio, fin);

        double ingreso = totalIngresoBD != null ? totalIngresoBD.doubleValue() : 0.0;
        double gasto = totalGastoBD != null ? totalGastoBD.doubleValue() : 0.0;

        // 2. Obtener Nivel de Endeudamiento del último análisis
        List<Analisis> historial = analisisRepository.findByUsuarioIdOrderByFechaAnalisisDesc(usuarioId);
        double nivelEndeudamiento = 0.0;
        if (!historial.isEmpty() && historial.get(0).getNivelEndeudamiento() != null) {
            nivelEndeudamiento = historial.get(0).getNivelEndeudamiento().doubleValue();
        }

        // Si no hay ingreso registrado en transacciones pero sí en el último análisis
        if (ingreso <= 0 && !historial.isEmpty() && historial.get(0).getIngresoMensual() != null) {
            ingreso = historial.get(0).getIngresoMensual().doubleValue();
        }

        // ==========================
        // 1. Alerta de Liquidez
        // ==========================
        if (ingreso > 0) {
            double ratioGasto = (gasto / ingreso) * 100.0;
            if (ratioGasto >= 90.0) {
                alertas.add(AlertaDTO.builder()
                        .tipo("Liquidez")
                        .nivel("Crítico")
                        .mensaje(String.format("Tus gastos representan el %.1f%% de tus ingresos mensuales.", ratioGasto))
                        .accion("Reducir egresos no esenciales urgentemente")
                        .build());
            } else if (ratioGasto >= 70.0) {
                alertas.add(AlertaDTO.builder()
                        .tipo("Liquidez")
                        .nivel("Advertencia")
                        .mensaje(String.format("Tus gastos representan el %.1f%% de tus ingresos mensuales.", ratioGasto))
                        .accion("Monitorear compras y gastos variables")
                        .build());
            }
        }

        // ==========================
        // 2. Alerta de Ahorro
        // ==========================
        if (ingreso > 0) {
            double ahorro = ingreso - gasto;
            double capacidadAhorro = (ahorro / ingreso) * 100.0;

            if (ahorro < 0) {
                alertas.add(AlertaDTO.builder()
                        .tipo("Ahorro")
                        .nivel("Crítico")
                        .mensaje(String.format("Tus gastos superan tus ingresos por $%.2f este período.", Math.abs(ahorro)))
                        .accion("Ajustar presupuesto para evitar déficit")
                        .build());
            } else if (capacidadAhorro < 10.0) {
                alertas.add(AlertaDTO.builder()
                        .tipo("Ahorro")
                        .nivel("Advertencia")
                        .mensaje(String.format("Tu margen de ahorro es del %.1f%% (inferior al 10%% recomendado).", capacidadAhorro))
                        .accion("Apartar un porcentaje antes de gastar")
                        .build());
            }
        }

        // ==========================
        // 3. Alerta de Endeudamiento
        // ==========================
        if (nivelEndeudamiento >= 80.0) {
            alertas.add(AlertaDTO.builder()
                    .tipo("Endeudamiento")
                    .nivel("Crítico")
                    .mensaje(String.format("Tu nivel de endeudamiento es muy elevado (%.1f%%).", nivelEndeudamiento))
                    .accion("Priorizar pago de cuotas y congelar deudas")
                    .build());
        } else if (nivelEndeudamiento >= 60.0) {
            alertas.add(AlertaDTO.builder()
                    .tipo("Endeudamiento")
                    .nivel("Advertencia")
                    .mensaje(String.format("Tu nivel de endeudamiento es alto (%.1f%%).", nivelEndeudamiento))
                    .accion("Evitar contratar nuevas líneas de crédito")
                    .build());
        }

        // ==========================
        // 4. Concentración de Gasto
        // ==========================
        try {
            Map<String, Double> distribucion = distribucionGastosService.obtenerPorPeriodo(
                    usuarioId, ym.toString());

            if (distribucion != null && !distribucion.isEmpty() && gasto > 0) {
                for (Map.Entry<String, Double> entry : distribucion.entrySet()) {
                    double pct = (entry.getValue() / gasto) * 100.0;
                    if (pct >= 40.0) {
                        alertas.add(AlertaDTO.builder()
                                .tipo("Patrón de consumo")
                                .nivel(pct >= 60.0 ? "Advertencia" : "Información")
                                .mensaje(String.format("El %.1f%% de tus gastos se concentra en '%s'.", pct, entry.getKey()))
                                .categoria(entry.getKey())
                                .accion("Diversificar y auditar movimientos de esta categoría")
                                .build());
                    }
                }
            }
        } catch (Exception ignored) {}

        // ==========================
        // 5. Alerta de Gasto Individual Elevado
        // ==========================
        if (ingreso > 0) {
            List<Transaccion> transaccionesMes = transaccionRepository.findByUsuarioIdAndFechaBetween(usuarioId, inicio, fin);
            for (Transaccion t : transaccionesMes) {
                if (t.getTipo() == TipoTransaccion.GASTO && t.getValor() != null) {
                    double val = t.getValor().doubleValue();
                    double pctGasto = (val / ingreso) * 100.0;
                    if (pctGasto >= 30.0) {
                        alertas.add(AlertaDTO.builder()
                                .tipo("Gasto Elevado")
                                .nivel(pctGasto >= 50.0 ? "Crítico" : "Advertencia")
                                .mensaje(String.format("Gasto individual significativo: '%s' por $%.2f (%.1f%% de tu ingreso).",
                                        t.getDescripcion(), val, pctGasto))
                                .accion("Verificar si fue un imprevisto planificado")
                                .build());
                    }
                }
            }
        }

        return alertas;
    }
}
