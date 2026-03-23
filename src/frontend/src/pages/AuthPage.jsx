import { useState } from 'react'
import './AuthPage.css'

export default function AuthPage() {
  const [mode, setMode]         = useState('login')
  const [form, setForm]         = useState({ name: '', contact: '', password: '' })
  const [contactType, setContactType] = useState('email')
  const [submitted, setSubmitted] = useState(false)

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }))

  const handleSubmit = (e) => {
    e.preventDefault()
    setSubmitted(true)
  }

  if (submitted) {
    return (
      <div className="auth-page">
        <div className="auth-card">
          <div className="auth-success">
            <span className="success-icon">✓</span>
            <h2>{mode === 'login' ? 'Welcome back.' : 'Account created.'}</h2>
            <p>You're all set.</p>
            <button className="auth-btn" onClick={() => setSubmitted(false)}>Continue</button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="auth-page">
      <div className="auth-card">

        <div className="auth-header">
          <span className="auth-logo">LGT</span>
          <div className="auth-tabs">
            <button
              className={`auth-tab ${mode === 'login' ? 'active' : ''}`}
              onClick={() => setMode('login')}
            >Sign In</button>
            <button
              className={`auth-tab ${mode === 'register' ? 'active' : ''}`}
              onClick={() => setMode('register')}
            >Register</button>
          </div>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>

          {mode === 'register' && (
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <input
                className="form-input"
                type="text"
                placeholder="Jane Doe"
                value={form.name}
                onChange={e => set('name', e.target.value)}
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
                  onClick={() => setContactType('email')}
                >Email</button>
                <button
                  type="button"
                  className={`toggle-pill ${contactType === 'phone' ? 'active' : ''}`}
                  onClick={() => setContactType('phone')}
                >Phone</button>
              </div>
            </div>
            <input
              className="form-input"
              type={contactType === 'email' ? 'email' : 'tel'}
              placeholder={contactType === 'email' ? 'you@example.com' : '514-555-0100'}
              value={form.contact}
              onChange={e => set('contact', e.target.value)}
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
              onChange={e => set('password', e.target.value)}
              required
            />
          </div>

          <button type="submit" className="auth-btn">
            {mode === 'login' ? 'Sign In' : 'Create Account'}
          </button>

        </form>
      </div>
    </div>
  )
}
