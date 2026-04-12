import { describe, it, expect, beforeEach } from 'vitest'
import {
  getStoredAuthSession,
  persistAuthSession,
  clearAuthSession,
  isUnauthorizedError,
  AUTH_CHANGE_EVENT,
} from './authStorage'

const KEY = 'letsgettesty.auth'

beforeEach(() => {
  localStorage.clear()
})

describe('getStoredAuthSession', () => {
  it('returns null when nothing is stored', () => {
    expect(getStoredAuthSession()).toBeNull()
  })

  it('returns the session when a valid session is stored', () => {
    const session = { token: 'abc123', role: 'USER' }
    localStorage.setItem(KEY, JSON.stringify(session))
    expect(getStoredAuthSession()).toEqual(session)
  })

  it('returns null when token is an empty string', () => {
    localStorage.setItem(KEY, JSON.stringify({ token: '' }))
    expect(getStoredAuthSession()).toBeNull()
  })

  it('returns null when token is only whitespace', () => {
    localStorage.setItem(KEY, JSON.stringify({ token: '   ' }))
    expect(getStoredAuthSession()).toBeNull()
  })

  it('returns null when token is not a string', () => {
    localStorage.setItem(KEY, JSON.stringify({ token: 123 }))
    expect(getStoredAuthSession()).toBeNull()
  })

  it('returns null when stored JSON is malformed', () => {
    localStorage.setItem(KEY, 'not-valid-json')
    expect(getStoredAuthSession()).toBeNull()
  })

  it('returns null when stored value is the string "null"', () => {
    localStorage.setItem(KEY, 'null')
    expect(getStoredAuthSession()).toBeNull()
  })
})

describe('persistAuthSession', () => {
  it('writes the session to localStorage', () => {
    const session = { token: 'tok123', role: 'USER' }
    persistAuthSession(session)
    expect(JSON.parse(localStorage.getItem(KEY))).toEqual(session)
  })

  it('dispatches an auth-change event', () => {
    let fired = false
    window.addEventListener(AUTH_CHANGE_EVENT, () => { fired = true }, { once: true })
    persistAuthSession({ token: 'tok' })
    expect(fired).toBe(true)
  })

  it('overwrites a previously stored session', () => {
    persistAuthSession({ token: 'old' })
    persistAuthSession({ token: 'new' })
    expect(JSON.parse(localStorage.getItem(KEY)).token).toBe('new')
  })
})

describe('clearAuthSession', () => {
  it('removes the session from localStorage', () => {
    localStorage.setItem(KEY, JSON.stringify({ token: 'tok' }))
    clearAuthSession()
    expect(localStorage.getItem(KEY)).toBeNull()
  })

  it('dispatches an auth-change event', () => {
    let fired = false
    window.addEventListener(AUTH_CHANGE_EVENT, () => { fired = true }, { once: true })
    clearAuthSession()
    expect(fired).toBe(true)
  })

  it('does not throw when nothing is stored', () => {
    expect(() => clearAuthSession()).not.toThrow()
  })
})

describe('isUnauthorizedError', () => {
  it('returns true for status 401', () => {
    expect(isUnauthorizedError({ status: 401 })).toBe(true)
  })

  it('returns false for status 403', () => {
    expect(isUnauthorizedError({ status: 403 })).toBe(false)
  })

  it('returns false for status 500', () => {
    expect(isUnauthorizedError({ status: 500 })).toBe(false)
  })

  it('returns false for null', () => {
    expect(isUnauthorizedError(null)).toBe(false)
  })

  it('returns false for undefined', () => {
    expect(isUnauthorizedError(undefined)).toBe(false)
  })

  it('returns false when error has no status property', () => {
    expect(isUnauthorizedError(new Error('oops'))).toBe(false)
  })
})
