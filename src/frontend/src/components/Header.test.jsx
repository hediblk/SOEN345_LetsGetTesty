import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, vi } from 'vitest'
import Header from './Header'

function renderHeader(authSession, onLogout = vi.fn(), initialPath = '/') {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <Header authSession={authSession} onLogout={onLogout} />
    </MemoryRouter>
  )
}

describe('Header — unauthenticated (no session)', () => {
  it('shows the Customer link', () => {
    renderHeader(null)
    expect(screen.getByText('Customer')).toBeInTheDocument()
  })

  it('shows the Admin link', () => {
    renderHeader(null)
    expect(screen.getByText('Admin')).toBeInTheDocument()
  })

  it('does not show the Logout button', () => {
    renderHeader(null)
    expect(screen.queryByText('Logout')).not.toBeInTheDocument()
  })

  it('does not show role-specific nav links', () => {
    renderHeader(null)
    expect(screen.queryByText('Events')).not.toBeInTheDocument()
    expect(screen.queryByText('My Tickets')).not.toBeInTheDocument()
    expect(screen.queryByText('Add Event')).not.toBeInTheDocument()
    expect(screen.queryByText('Edit Event')).not.toBeInTheDocument()
  })
})

describe('Header — USER role', () => {
  const session = { role: 'USER', token: 'tok' }

  it('shows the Events link', () => {
    renderHeader(session)
    expect(screen.getByText('Events')).toBeInTheDocument()
  })

  it('shows the My Tickets link', () => {
    renderHeader(session)
    expect(screen.getByText('My Tickets')).toBeInTheDocument()
  })

  it('shows the Logout button', () => {
    renderHeader(session)
    expect(screen.getByText('Logout')).toBeInTheDocument()
  })

  it('does not show admin-only links', () => {
    renderHeader(session)
    expect(screen.queryByText('Add Event')).not.toBeInTheDocument()
    expect(screen.queryByText('Edit Event')).not.toBeInTheDocument()
  })

  it('calls onLogout when Logout is clicked', () => {
    const onLogout = vi.fn()
    renderHeader(session, onLogout)
    fireEvent.click(screen.getByText('Logout'))
    expect(onLogout).toHaveBeenCalledOnce()
  })
})

describe('Header — ADMIN role', () => {
  const session = { role: 'ADMIN', token: 'tok' }

  it('shows the Add Event link', () => {
    renderHeader(session)
    expect(screen.getByText('Add Event')).toBeInTheDocument()
  })

  it('shows the Edit Event link', () => {
    renderHeader(session)
    expect(screen.getByText('Edit Event')).toBeInTheDocument()
  })

  it('shows the Logout button', () => {
    renderHeader(session)
    expect(screen.getByText('Logout')).toBeInTheDocument()
  })

  it('does not show user-only links', () => {
    renderHeader(session)
    expect(screen.queryByText('Events')).not.toBeInTheDocument()
    expect(screen.queryByText('My Tickets')).not.toBeInTheDocument()
  })
})

describe('Header — active link highlighting', () => {
  it('marks the current path link as active for USER', () => {
    renderHeader({ role: 'USER', token: 'tok' }, vi.fn(), '/reservations')
    expect(screen.getByText('My Tickets')).toHaveClass('active')
  })

  it('does not mark non-current links as active', () => {
    renderHeader({ role: 'USER', token: 'tok' }, vi.fn(), '/reservations')
    expect(screen.getByText('Events')).not.toHaveClass('active')
  })

  it('marks Add Event as active when on that path', () => {
    renderHeader({ role: 'ADMIN', token: 'tok' }, vi.fn(), '/admin/add-events')
    expect(screen.getByText('Add Event')).toHaveClass('active')
  })
})

describe('Header — logo', () => {
  it('renders the LetsGetTesty logo', () => {
    renderHeader(null)
    expect(screen.getByText('LetsGetTesty')).toBeInTheDocument()
  })
})
