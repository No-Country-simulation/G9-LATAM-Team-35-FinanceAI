import { request } from './api'

export const transaccionesService = {
  obtenerTransacciones() {
    return request('/api/transacciones')
  },
  obtenerPorTipo(tipo) {
    return request(`/api/transacciones/tipo?tipo=${tipo}`)
  },
  registrarTransaccion(datos) {
    return request('/api/transacciones', {
      method: 'POST',
      body: JSON.stringify(datos),
    })
  },
  eliminarTransaccion(id) {
    return request(`/api/transacciones/${id}`, {
      method: 'DELETE',
    })
  },
  editarTransaccion(id, datos) {
    return request(`/api/transacciones/${id}`, {
      method: 'PUT',
      body: JSON.stringify(datos),
    })
  },
}
