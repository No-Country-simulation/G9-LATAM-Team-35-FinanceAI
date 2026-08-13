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

  /**
   * GET /api/transacciones/ingreso-mensual?mes={1-12}&anio={yyyy}
   * Endpoint 4.1 — Calcula el ingreso mensual del usuario.
   * Respuesta: { ingreso_mensual, tiene_datos, mensaje }
   */
  calcularIngresoMensual(mes, anio) {
    return request(`/api/transacciones/ingreso-mensual?mes=${mes}&anio=${anio}`)
  },

  /**
   * GET /api/transacciones/sin-clasificar
   * Obtiene transacciones sin categoría asignada.
   */
  obtenerSinClasificar() {
    return request('/api/transacciones/sin-clasificar')
  },

  /**
   * GET /api/transacciones/buscar?descripcion={text}
   * Búsqueda de transacciones por descripción (backend, no local).
   */
  buscarPorDescripcion(descripcion) {
    return request(`/api/transacciones/buscar?descripcion=${encodeURIComponent(descripcion)}`)
  },
}
