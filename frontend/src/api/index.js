const API_BASE = ''

function getToken() {
  return sessionStorage.getItem('token') || localStorage.getItem('token')
}

export function authHeader() {
  const token = getToken()
  return token ? { Authorization: 'Bearer ' + token } : {}
}

export async function safeJson(res) {
  const text = await res.text()
  if (!text) return {}
  try {
    return JSON.parse(text)
  } catch {
    return {}
  }
}

export function api(endpoint, options = {}) {
  const token = getToken()
  const headers = { ...options.headers }
  if (token) {
    headers['Authorization'] = 'Bearer ' + token
  }
  if (!(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json'
  }
  return fetch(API_BASE + endpoint, {
    method: options.method || 'GET',
    headers,
    body: options.body
  }).then(async res => {
    if (res.status === 401) {
      sessionStorage.removeItem('token')
      localStorage.removeItem('token')
      sessionStorage.removeItem('userInfo')
      localStorage.removeItem('userInfo')
      window.location.href = '/#/login'
      return Promise.reject(new Error('Unauthorized'))
    }
    return safeJson(res)
  })
}

export function login(username, password) {
  return fetch(API_BASE + '/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  }).then(safeJson)
}
