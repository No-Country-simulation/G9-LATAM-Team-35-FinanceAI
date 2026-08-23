const BASE_URL = 'https://g9-latam-team-35-financeai-1.onrender.com'
//const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
export async function request(endpoint, options = {}) {
  const token = localStorage.getItem('token')
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  }

  const config = {
    ...options,
    headers,
  }

  try {
    const response = await fetch(`${BASE_URL}${endpoint}`, config)
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      throw new Error(errorData.mensaje || errorData.message || `Error ${response.status}: ${response.statusText}`)
    }
    return await response.json().catch(() => ({}))
  } catch (error) {
    console.warn(`[API Call to ${endpoint} failed]:`, error.message)
    throw error
  }
}
