import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { persistAuthSession } from '../auth/authStorage'
import './AuthPage.css'

function validateRegisterPassword(password) {
  if (password.length < 6) {
    return 'Password must be at least 6 characters long.'
  }
  if (!/[a-z]/.test(password)) {
    return 'Password must include a lowercase letter.'
  }
  if (!/[A-Z]/.test(password)) {
    return 'Password must include an uppercase letter.'
  }
  if (!/[0-9]/.test(password)) {
    return 'Password must include a number.'
  }
  if (!/[^A-Za-z0-9]/.test(password)) {
    return 'Password must include a special character.'
  }
  return ''
}

async function submitAuthRequest(path, payload) {
  let response

  try {
    response = await fetch(path, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })
  } catch {
    throw new Error('Unable to reach the backend. Make sure it is running on port 8080.')
  }

  const data = await response.json().catch(() => null)

  if (!response.ok) {
    throw new Error(data?.message || 'Request failed.')
  }

  return data
}

function getPostLoginPath(role) {
  return role === 'ADMIN' ? '/admin/add-events' : '/'
}

export default function AuthPage({ onAuthSuccess, authRole = 'USER', allowRegister = authRole === 'USER' }) {
  const navigate = useNavigate()
  const isAdminAuth = authRole === 'ADMIN'
  const [mode, setMode]         = useState('login')
  const [form, setForm]         = useState({ name: '', contact: '', password: '' })
  const [contactType, setContactType] = useState('email')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [passwordError, setPasswordError] = useState('')

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }))

  const handleModeChange = (nextMode) => {
    setMode(nextMode)
    setError('')
    setPasswordError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setPasswordError('')

    if (allowRegister && mode === 'register') {
      const validationError = validateRegisterPassword(form.password)
      if (validationError) {
        setPasswordError(validationError)
        return
      }
    }

    setSubmitting(true)

    try {
      const payload = mode === 'login'
        ? {
            contact: form.contact,
            contactType,
            password: form.password,
          }
        : {
            name: form.name,
            contact: form.contact,
            contactType,
            password: form.password,
          }

      const path = mode === 'login'
        ? (isAdminAuth ? '/api/auth/admin/login' : '/api/auth/login')
        : '/api/auth/register'

      const data = await submitAuthRequest(path, payload)

      persistAuthSession(data)
      onAuthSuccess?.(data)
      navigate(getPostLoginPath(data.role || authRole), { replace: true })
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">

        <div className="auth-header">
          <span className="auth-logo">{isAdminAuth ? 'LGT Admin' : 'LGT'}</span>
          {allowRegister ? (
            <div className="auth-tabs">
              <button
                type="button"
                className={`auth-tab ${mode === 'login' ? 'active' : ''}`}
                onClick={() => handleModeChange('login')}
              >Sign In</button>
              <button
                type="button"
                className={`auth-tab ${mode === 'register' ? 'active' : ''}`}
                onClick={() => handleModeChange('register')}
              >Register</button>
            </div>
          ) : (
            <span className="auth-mode-label">Administrator</span>
          )}
        </div>

        <p className="auth-intro">
          {isAdminAuth ? 'Use your preconfigured admin account to manage events.' : 'Sign in as a customer or create a new account.'}
        </p>

        <form className="auth-form" onSubmit={handleSubmit}>

          {allowRegister && mode === 'register' && (
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <input
                className="form-input"
                type="text"
                placeholder="Jane Doe"
                value={form.name}
                onChange={e => {
                  set('name', e.target.value)
                  setError('')
                }}
                disabled={submitting}
                required
              />
            </div>
          )}

          <div className="form-group">
            <div className="contact-toggle">
              <label className="form-label">Contact</label>
              <div className="toggle-pills">
                <button
                  type="button"
                  className={`toggle-pill ${contactType === 'email' ? 'active' : ''}`}
                  onClick={() => {
                    setContactType('email')
                    setError('')
                  }}
                  disabled={submitting}
                >Email</button>
                <button
                  type="button"
                  className={`toggle-pill ${contactType === 'phone' ? 'active' : ''}`}
                  onClick={() => {
                    setContactType('phone')
                    setError('')
                  }}
                  disabled={submitting}
                >Phone</button>
              </div>
            </div>
            <input
              className="form-input"
              type={contactType === 'email' ? 'email' : 'tel'}
              placeholder={contactType === 'email' ? 'you@example.com' : '514-555-0100'}
              value={form.contact}
              onChange={e => {
                set('contact', e.target.value)
                setError('')
              }}
              disabled={submitting}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Password</label>
            <input
              className="form-input"
              type="password"
              placeholder="••••••••"
              value={form.password}
                onChange={e => {
                  set('password', e.target.value)
                  setError('')
                  setPasswordError('')
                }}
                disabled={submitting}
                required
              />
              {allowRegister && mode === 'register' && passwordError ? <p className="field-error">{passwordError}</p> : null}
          </div>

          {error && <p className="auth-error">{error}</p>}

          <button type="submit" className="auth-btn" disabled={submitting}>
            {submitting
              ? (mode === 'login' ? 'Signing In…' : 'Creating Account…')
              : (mode === 'login' ? (isAdminAuth ? 'Admin Sign In' : 'Sign In') : 'Create Account')}
          </button>

        </form>
      </div>
    </div>
  )
}
