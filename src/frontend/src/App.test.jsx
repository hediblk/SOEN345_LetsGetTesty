import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('./api/eventsApi', () => ({ fetchEvents: vi.fn().mockResolvedValue([]) }))
vi.mock('./api/reservationsApi', () => ({
  fetchReservationsByUser: vi.fn().mockResolvedValue([]),
  createReservation: vi.fn(),
}))
vi.mock('./auth/authStorage', () => ({
  getStoredAuthSession: vi.fn(),
  clearAuthSession: vi.fn(),
  persistAuthSession: vi.fn(),
  isUnauthorizedError: vi.fn().mockReturnValue(false),
  AUTH_CHANGE_EVENT: 'letsgettesty:auth-change',
}))
vi.mock('./pages/HomePage', () => ({ default: () => 'HomePage' }))
vi.mock('./pages/AddEventsPage', () => ({ default: () => 'AddEventsPage' }))
vi.mock('./pages/EditEventsPage', () => ({ default: () => 'EditEventsPage' }))
vi.mock('./pages/AuthPage', () => ({ default: () => 'AuthPage' }))
vi.mock('./pages/ReservationsPage', () => ({ default: () => 'ReservationsPage' }))
vi.mock('./components/Header', () => ({ default: () => null }))

import { getStoredAuthSession } from './auth/authStorage'
import App from './App'

function navigate(path) {
  window.history.pushState({}, '', path)
}

describe('App — unauthenticated routing', () => {
  beforeEach(() => {
    getStoredAuthSession.mockReturnValue(null)
  })

  it('redirects / to /auth when not authenticated', () => {
    navigate('/')
    render(<App />)
    expect(screen.getByText('AuthPage')).toBeInTheDocument()
  })

  it('shows AuthPage at /auth', () => {
    navigate('/auth')
    render(<App />)
    expect(screen.getByText('AuthPage')).toBeInTheDocument()
  })

  it('shows admin AuthPage at /admin', () => {
    navigate('/admin')
    render(<App />)
    expect(screen.getByText('AuthPage')).toBeInTheDocument()
  })

  it('redirects /admin/add-events to /admin login when not authenticated', () => {
    navigate('/admin/add-events')
    render(<App />)
    expect(screen.getByText('AuthPage')).toBeInTheDocument()
  })

  it('redirects unknown route to /auth when not authenticated', () => {
    navigate('/unknown-route')
    render(<App />)
    expect(screen.getByText('AuthPage')).toBeInTheDocument()
  })
})

describe('App — USER routing', () => {
  beforeEach(() => {
    getStoredAuthSession.mockReturnValue({ id: 1, token: 'user-tok', role: 'USER' })
  })

  it('shows HomePage at / for USER', () => {
    navigate('/')
    render(<App />)
    expect(screen.getByText('HomePage')).toBeInTheDocument()
  })

  it('redirects /auth to / when USER is authenticated', () => {
    navigate('/auth')
    render(<App />)
    expect(screen.getByText('HomePage')).toBeInTheDocument()
  })

  it('shows ReservationsPage at /reservations for USER', () => {
    navigate('/reservations')
    render(<App />)
    expect(screen.getByText('ReservationsPage')).toBeInTheDocument()
  })

  it('redirects /admin/add-events to / for USER role', () => {
    navigate('/admin/add-events')
    render(<App />)
    expect(screen.getByText('HomePage')).toBeInTheDocument()
  })

  it('redirects /admin/edit-events to / for USER role', () => {
    navigate('/admin/edit-events')
    render(<App />)
    expect(screen.getByText('HomePage')).toBeInTheDocument()
  })
})

describe('App — ADMIN routing', () => {
  beforeEach(() => {
    getStoredAuthSession.mockReturnValue({ id: 2, token: 'admin-tok', role: 'ADMIN' })
  })

  it('redirects / to /admin/add-events for ADMIN', () => {
    navigate('/')
    render(<App />)
    expect(screen.getByText('AddEventsPage')).toBeInTheDocument()
  })

  it('shows AddEventsPage at /admin/add-events for ADMIN', () => {
    navigate('/admin/add-events')
    render(<App />)
    expect(screen.getByText('AddEventsPage')).toBeInTheDocument()
  })

  it('shows EditEventsPage at /admin/edit-events for ADMIN', () => {
    navigate('/admin/edit-events')
    render(<App />)
    expect(screen.getByText('EditEventsPage')).toBeInTheDocument()
  })

  it('redirects /auth to /admin/add-events when ADMIN is authenticated', () => {
    navigate('/auth')
    render(<App />)
    expect(screen.getByText('AddEventsPage')).toBeInTheDocument()
  })

  it('redirects / to /admin/add-events for ADMIN (not HomePage)', () => {
    navigate('/')
    render(<App />)
    expect(screen.queryByText('HomePage')).not.toBeInTheDocument()
  })
})
