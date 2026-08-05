import { request } from './api'

export const authService = {
  login(email, password) {
    return request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    })
  },
  register(nombre, email, password) {
    return request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ nombre, email, password }),
    })
  },
}
