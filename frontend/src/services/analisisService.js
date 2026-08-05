import { request } from './api'

export const analisisService = {
  analizarFinanzas(payload) {
    return request('/analisis-financiero', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  clasificarTransaccion(descripcion, monto) {
    return request('/clasificar-transaccion', {
      method: 'POST',
      body: JSON.stringify({ descripcion, monto }),
    })
  },
}
