import { request } from './api'

/**
 * Servicio para UsuarioController → /api/usuarios
 * El usuario se identifica automáticamente via JWT (token en localStorage).
 */
export const usuarioService = {
  /**
   * GET /api/usuarios/me
   * Obtiene el perfil del usuario autenticado.
   * Respuesta: { id, nombre, email, moneda, monedaTexto }
   */
  obtenerPerfil() {
    return request('/api/usuarios/me')
  },

  /**
   * PUT /api/usuarios/me
   * Actualiza nombre y/o moneda del usuario autenticado.
   * @param {string} nombre
   * @param {string} moneda - código de moneda, ej: "MXN", "USD"
   * Respuesta: { id, nombre, email, moneda, monedaTexto }
   */
  actualizarPerfil(nombre, moneda) {
    return request('/api/usuarios/me', {
      method: 'PUT',
      body: JSON.stringify({ nombre, moneda }),
    })
  },

  /**
   * GET /api/usuarios/monedas-disponibles
   * Lista las monedas disponibles para el selector de Configuración.
   * Respuesta: [{ codigo: "MXN", texto: "Peso Mexicano" }, ...]
   */
  listarMonedas() {
    return request('/api/usuarios/monedas-disponibles')
  },
}
