import { request } from './api'

export const analisisService = {
  analizarFinanzas(payload) {
    // POST /analisis-financiero (AnalisisFinancieroController)
    return request('/analisis-financiero', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },

  /**
   * Clasifica una o varias transacciones vía el endpoint correcto del frontend:
   * POST /api/transacciones/clasificar-transaccion  (TransaccionController)
   * Acepta un array de { descripcion, valor }.
   * Este endpoint está marcado como permitAll() en SecurityConfig.
   *
   * NO usar /clasificar-transaccion (sin /api/) — ese es el endpoint interno
   * de AnalisisFinancieroController, pensado para que el backend llame al modelo ML.
   */
  clasificarTransaccion(descripcion, monto) {
    return request('/api/transacciones/clasificar-transaccion', {
      method: 'POST',
      body: JSON.stringify([{ descripcion, valor: monto }]),
    }).then(res => {

      // El backend devuelve una lista; tomamos el primer (y único) resultado
      if (Array.isArray(res) && res.length > 0) return res[0]
      return res
    })
  },

  // ──────────────────────────────────────────────────────
  // Endpoint 5.1 y 5.2 — AnalisisController: /api/analisis
  // ──────────────────────────────────────────────────────

  /**
   * GET /api/analisis/historial
   * Obtiene el historial de análisis del usuario autenticado.
   * Respuesta: [{ id, perfil, probabilidad, ingresoMensual, nivelEndeudamiento,
   *               frecuenciaAhorro, fechaAnalisis, nombre }]
   * El campo `nombre` viene generado automáticamente: "Agosto 2026 - SALUDABLE"
   * El campo `perfil` es un enum: SALUDABLE | EN_OBSERVACION | EN_RIESGO...
   */
  obtenerHistorial() {
    return request('/api/analisis/historial')
  },

  /**
   * GET /api/analisis/buscar?query={text}
   * Busca análisis por nombre generado (fecha + perfil).
   */
  buscarAnalisis(query) {
    return request(`/api/analisis/buscar?query=${encodeURIComponent(query)}`)
  },

  calcularEndeudamiento(ingresoMensual, cuotasMensuales) {
    return request('/api/analisis/nivel-endeudamiento', {
      method: 'POST',
      body: JSON.stringify({
        ingresoMensual: Number(ingresoMensual),
        cuotasMensuales: cuotasMensuales.map(c => Number(c)),
      }),
    }).then(response => {
      
      // Si la respuesta tiene 'data', extraerla
      const data = response.data || response
      
      return {
        nivelEndeudamiento: Number(data.nivelEndeudamiento || data.nivel_endeudamiento || 0),
        totalDeudas: Number(data.totalDeudas || data.total_deudas || 0),
        ingresoMensual: Number(data.ingresoMensual || data.ingreso_mensual || 0),
        mensaje: data.mensaje || 'Cálculo completado'
      }
    })
  }
}
