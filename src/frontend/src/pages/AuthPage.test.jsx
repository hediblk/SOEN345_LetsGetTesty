import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import AuthPage from './AuthPage'

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...actual, useNavigate: () => vi.fn() }
})

vi.mock('../auth/authStorage', () => ({
  persistAuthSession: vi.fn(),
}))

function renderAuth(props = {}) {
  return render(
    <MemoryRouter>
      <AuthPage {...props} />
    </MemoryRouter>
  )
}

describe('AuthPage — default (login mode, USER)', () => {
  it('renders the LGT logo', () => {
    renderAuth()
    expect(screen.getByText('LGT')).toBeInTheDocument()
  })

  it('shows Sign In and Register tabs', () => {
    renderAuth()
    // Both the tab and submit button say "Sign In", so use getAllByText
    expect(screen.getAllByText('Sign In').length).toBeGreaterThan(0)
    expect(screen.getByText('Register')).toBeInTheDocument()
  })

  it('shows a Sign In submit button', () => {
    renderAuth()
    // Both the tab and the submit button have "Sign In" text
    const signInButtons = screen.getAllByRole('button', { name: /^sign in$/i })
    const submitBtn = signInButtons.find(b => b.type === 'submit')
    expect(submitBtn).toBeTruthy()
  })

  it('shows email and password fields', () => {
    renderAuth()
    expect(screen.getByPlaceholderText('you@example.com')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('••••••••')).toBeInTheDocument()
  })
})

describe('AuthPage — admin mode', () => {
  it('shows LGT Admin logo', () => {
    renderAuth({ authRole: 'ADMIN', allowRegister: false })
    expect(screen.getByText('LGT Admin')).toBeInTheDocument()
  })

  it('shows Administrator label instead of tabs', () => {
    renderAuth({ authRole: 'ADMIN', allowRegister: false })
    expect(screen.getByText('Administrator')).toBeInTheDocument()
    expect(screen.queryByText('Register')).not.toBeInTheDocument()
  })

  it('shows Admin Sign In button', () => {
    renderAuth({ authRole: 'ADMIN', allowRegister: false })
    expect(screen.getByRole('button', { name: /admin sign in/i })).toBeInTheDocument()
  })
})

describe('AuthPage — switching to register mode', () => {
  beforeEach(() => {
    renderAuth({ allowRegister: true })
    fireEvent.click(screen.getByText('Register'))
  })

  it('shows Full Name field', () => {
    expect(screen.getByPlaceholderText('Jane Doe')).toBeInTheDocument()
  })

  it('shows Create Account submit button', () => {
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument()
  })
})

describe('AuthPage — password validation (register mode)', () => {
  beforeEach(() => {
    renderAuth({ allowRegister: true })
    fireEvent.click(screen.getByText('Register'))
  })

  it('shows error when password is too short', async () => {
    fireEvent.change(screen.getByPlaceholderText('Jane Doe'), { target: { value: 'Jane' } })
    fireEvent.change(screen.getByPlaceholderText('you@example.com'), { target: { value: 'test@test.com' } })
    fireEvent.change(screen.getByPlaceholderText('••••••••'), { target: { value: 'abc' } })
    fireEvent.click(screen.getByRole('button', { name: /create account/i }))
    await waitFor(() => {
      expect(screen.getByText(/at least 6 characters/i)).toBeInTheDocument()
    })
  })

  it('shows error when password has no lowercase letter', async () => {
    fireEvent.change(screen.getByPlaceholderText('Jane Doe'), { target: { value: 'Jane' } })
    fireEvent.change(screen.getByPlaceholderText('you@example.com'), { target: { value: 'test@test.com' } })
    fireEvent.change(screen.getByPlaceholderText('••••••••'), { target: { value: 'ABC123!' } })
    fireEvent.click(screen.getByRole('button', { name: /create account/i }))
    await waitFor(() => {
      expect(screen.getByText(/lowercase letter/i)).toBeInTheDocument()
    })
  })

  it('shows error when password has no uppercase letter', async () => {
    fireEvent.change(screen.getByPlaceholderText('Jane Doe'), { target: { value: 'Jane' } })
    fireEvent.change(screen.getByPlaceholderText('you@example.com'), { target: { value: 'test@test.com' } })
    fireEvent.change(screen.getByPlaceholderText('••••••••'), { target: { value: 'abc123!' } })
    fireEvent.click(screen.getByRole('button', { name: /create account/i }))
    await waitFor(() => {
      expect(screen.getByText(/uppercase letter/i)).toBeInTheDocument()
    })
  })

  it('shows error when password has no number', async () => {
    fireEvent.change(screen.getByPlaceholderText('Jane Doe'), { target: { value: 'Jane' } })
    fireEvent.change(screen.getByPlaceholderText('you@example.com'), { target: { value: 'test@test.com' } })
    fireEvent.change(screen.getByPlaceholderText('••••••••'), { target: { value: 'Abcdef!' } })
    fireEvent.click(screen.getByRole('button', { name: /create account/i }))
    await waitFor(() => {
      expect(screen.getByText(/must include a number/i)).toBeInTheDocument()
    })
  })

  it('shows error when password has no special character', async () => {
    fireEvent.change(screen.getByPlaceholderText('Jane Doe'), { target: { value: 'Jane' } })
    fireEvent.change(screen.getByPlaceholderText('you@example.com'), { target: { value: 'test@test.com' } })
    fireEvent.change(screen.getByPlaceholderText('••••••••'), { target: { value: 'Abcdef1' } })
    fireEvent.click(screen.getByRole('button', { name: /create account/i }))
    await waitFor(() => {
      expect(screen.getByText(/special character/i)).toBeInTheDocument()
    })
  })

  it('does not show password error in login mode', () => {
    fireEvent.click(screen.getByText('Sign In'))
    expect(screen.queryByText(/at least 6 characters/i)).not.toBeInTheDocument()
  })
})

describe('AuthPage — contact type toggle', () => {
  it('shows email input by default', () => {
    renderAuth()
    expect(screen.getByPlaceholderText('you@example.com')).toHaveAttribute('type', 'email')
  })

  it('switches to phone input when Phone is selected', () => {
    renderAuth()
    fireEvent.click(screen.getByText('Phone'))
    expect(screen.getByPlaceholderText('514-555-0100')).toHaveAttribute('type', 'tel')
  })

  it('switches back to email when Email is selected', () => {
    renderAuth()
    fireEvent.click(screen.getByText('Phone'))
    fireEvent.click(screen.getByText('Email'))
    expect(screen.getByPlaceholderText('you@example.com')).toHaveAttribute('type', 'email')
  })
})

describe('AuthPage — mode switching clears errors', () => {
  it('clears errors when switching between login and register', async () => {
    renderAuth({ allowRegister: true })
    fireEvent.click(screen.getByText('Register'))
    fireEvent.change(screen.getByPlaceholderText('Jane Doe'), { target: { value: 'Jane' } })
    fireEvent.change(screen.getByPlaceholderText('you@example.com'), { target: { value: 'test@test.com' } })
    fireEvent.change(screen.getByPlaceholderText('••••••••'), { target: { value: 'abc' } })
    fireEvent.click(screen.getByRole('button', { name: /create account/i }))
    await waitFor(() => {
      expect(screen.getByText(/at least 6 characters/i)).toBeInTheDocument()
    })
    fireEvent.click(screen.getAllByText('Sign In')[0])
    expect(screen.queryByText(/at least 6 characters/i)).not.toBeInTheDocument()
  })
})
