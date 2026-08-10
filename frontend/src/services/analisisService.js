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
   * Acepta un array de { descripcion, monto }.
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
}
