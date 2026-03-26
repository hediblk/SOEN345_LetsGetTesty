const AUTH_STORAGE_KEY = 'letsgettesty.auth'
const AUTH_CHANGE_EVENT = 'letsgettesty:auth-change'

export function getStoredAuthSession() {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY)
    if (!raw) {
      return null
    }

    const session = JSON.parse(raw)
    if (!session || typeof session.token !== 'string' || session.token.trim() === '') {
      return null
    }

    return session
  } catch {
    return null
  }
}

export function persistAuthSession(session) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session))
  window.dispatchEvent(new Event(AUTH_CHANGE_EVENT))
}

export function clearAuthSession() {
  localStorage.removeItem(AUTH_STORAGE_KEY)
  window.dispatchEvent(new Event(AUTH_CHANGE_EVENT))
}

export function isUnauthorizedError(error) {
  return error?.status === 401
}

export { AUTH_CHANGE_EVENT }
