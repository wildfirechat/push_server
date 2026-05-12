const API_BASE = ''

function getToken() {
  return localStorage.getItem('admin_token')
}

export async function api(url, options = {}) {
  options.headers = options.headers || {}
  const token = getToken()
  if (token) {
    options.headers['Authorization'] = 'Bearer ' + token
  }
  if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
    options.headers['Content-Type'] = 'application/json'
    options.body = JSON.stringify(options.body)
  }

  const res = await fetch(API_BASE + url, options)
  if (res.status === 401) {
    localStorage.removeItem('admin_token')
    window.location.href = '/admin/'
    throw new Error('Unauthorized')
  }
  if (!res.ok) {
    throw new Error('HTTP ' + res.status)
  }
  return res.json()
}
