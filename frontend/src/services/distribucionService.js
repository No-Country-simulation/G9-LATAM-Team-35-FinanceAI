import { request } from './api'

/**
 * Servicio para DistribucionGastosController → /api/distribucion-gastos
 * El usuario se identifica automáticamente via JWT (token en localStorage).
 */
export const distribucionService = {
  /**
   * GET /api/distribucion-gastos?mes={yyyy-MM}
   * Devuelve gastos agrupados por categoría para la gráfica doughnut.
   * @param {string} mes - formato "yyyy-MM", ej: "2026-08"
   * Respuesta: { "ALIMENTACIÓN": 142.30, "TRANSPORTE": 12.50, ... } o null si no hay datos
   */
  obtenerDistribucion(mes) {
    return request(`/api/distribucion-gastos?mes=${mes}`)
  },
}
